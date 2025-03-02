package com.tqhit.adlib.sdk.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.tqhit.adlib.sdk.analytics.AnalyticsTracker
import com.tqhit.adlib.sdk.firebase.FirebaseRemoteConfigHelper
import com.tqhit.adlib.sdk.utils.Constant
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOpenHelper @Inject constructor(
    private val admobConsentHelper: AdmobConsentHelper,
    private val analyticsTracker: AnalyticsTracker,
    private val remoteConfigHelper: FirebaseRemoteConfigHelper
) {
    private var loadTime: Long = 0
    private var adUnitId = ""
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false
    private var isFirstOpen = false

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    fun setAdUnitId(adUnitId: String) {
        this.adUnitId = adUnitId
    }

    fun loadAd(context: Context) {
        if (!admobConsentHelper.canRequestAds()) return
        if (isLoadingAd || isAdAvailable()) return
        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context, if (Constant.DEBUG_MODE) Constant.ADMOB_AOA_AD_UNIT_ID else adUnitId, request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                }
            }
        )
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /** Check if ad exists and can be shown.  */
    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    /** Shows the ad if one isn't already showing.  */
    fun showAdIfAvailable(activity: Activity, adCallback: OnShowAdCompleteListener) {
        if (!isFirstOpen) {
            loadAd(activity)
            isFirstOpen = true
            return
        }

        if (!remoteConfigHelper.getBoolean("APP_OPEN_AD")) return

        if (activity is AdActivity) return

        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) {
            return
        }

        // If the app open ad is not available yet, invoke the callback then load the ad.
        if (!isAdAvailable()) {
            adCallback.onShowAdComplete()
            loadAd(activity)
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                adCallback.onShowAdComplete()
                loadAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                adCallback.onShowAdComplete()
                loadAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                analyticsTracker.trackEvent("aj_app_open_displayed")
            }
        }
        isShowingAd = true
        appOpenAd?.show(activity)
    }
}