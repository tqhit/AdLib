package com.tqhit.adlib.sdk.ads.applovin

import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAppOpenAd
import com.tqhit.adlib.sdk.analytics.AnalyticsTracker
import com.tqhit.adlib.sdk.data.local.PreferencesHelper
import com.tqhit.adlib.sdk.firebase.FirebaseRemoteConfigHelper
import com.tqhit.adlib.sdk.utils.Constant
import com.tqhit.adlib.sdk.ads.AdFrequencyManager
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaxAppOpenHelper @Inject constructor(
    private val analyticsTracker: AnalyticsTracker,
    private val remoteConfigHelper: FirebaseRemoteConfigHelper,
    private val preferencesHelper: PreferencesHelper,
    private val adFrequencyManager: AdFrequencyManager
) {
    private fun isAdEnabled() =
        remoteConfigHelper.getBoolean("aoa_enable")
                && !preferencesHelper.getBoolean(Constant.IS_PREMIUM, false)
    }
    private var loadTime: Long = 0
    private var adUnitId = ""
    private var appOpenAd: MaxAppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false

    val adLoaded = MutableLiveData<Boolean>()

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    fun setAdUnitId(adUnitId: String) {
        this.adUnitId = adUnitId
    }

    fun loadAd(context: Context) {
        if (!isAdEnabled()) return
        if (isLoadingAd || isAdAvailable()) return
        isLoadingAd = true
        analyticsTracker.logEvent("aj_app_open_load")
        val unitId = if (Constant.DEBUG_MODE) Constant.MAX_AOA_AD_UNIT_ID else adUnitId
        val ad = MaxAppOpenAd(unitId)
        ad.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd) {
                analyticsTracker.logEvent("aj_app_open_load_success")
                appOpenAd = ad as MaxAppOpenAd
                isLoadingAd = false
                loadTime = Date().time
                ad.setRevenueListener { maxAd ->
                    analyticsTracker.trackMaxRevenueEvent(maxAd)
                }
                adLoaded.postValue(true)
            }
            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                analyticsTracker.logEvent("aj_app_open_load_fail")
                isLoadingAd = false
                adLoaded.postValue(false)
            }
            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                analyticsTracker.logEvent("aj_app_open_show_fail")
                appOpenAd = null
                isShowingAd = false
                adLoaded.postValue(false)
                adFrequencyManager.recordAppOpenShown()
                currentCallback?.onShowAdComplete()
                currentCallback = null
                loadAd(context)
            }
            override fun onAdDisplayed(ad: MaxAd) {
                analyticsTracker.logEvent("aj_app_open_show_success")
            }
            override fun onAdHidden(ad: MaxAd) {
                analyticsTracker.logEvent("aj_app_open_close")
                appOpenAd = null
                isShowingAd = false
                adLoaded.postValue(false)
                adFrequencyManager.recordAppOpenShown()
                currentCallback?.onShowAdComplete()
                currentCallback = null
                loadAd(context)
            }
            override fun onAdClicked(ad: MaxAd) {
                analyticsTracker.logEvent("aj_app_open_click")
            }
        })
        ad.loadAd()
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

    private var currentCallback: OnShowAdCompleteListener? = null

    /** Shows the ad if one isn't already showing.  */
    fun showAdIfAvailable(activity: Activity, adCallback: OnShowAdCompleteListener) {
        if (!isAdEnabled() || isShowingAd) {
            adCallback.onShowAdComplete()
            return
        }

        // Frequency gating via AdFrequencyManager
        if (!adFrequencyManager.canShowAppOpen()) {
            adCallback.onShowAdComplete()
            return
        }

        if (!isAdAvailable()) {
            adCallback.onShowAdComplete()
            loadAd(activity)
            return
        }

        analyticsTracker.logEvent("aj_app_open_show")
        isShowingAd = true
        currentCallback = adCallback
        appOpenAd?.showAd()
    }
}


