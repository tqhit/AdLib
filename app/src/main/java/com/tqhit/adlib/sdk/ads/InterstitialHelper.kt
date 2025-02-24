package com.tqhit.adlib.sdk.ads

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.tqhit.adlib.sdk.ads.callback.InterstitialAdCallback
import com.tqhit.adlib.sdk.analytics.AnalyticsTracker
import com.tqhit.adlib.sdk.ui.dialog.LoadingAdsDialog
import com.tqhit.adlib.sdk.utils.Constant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterstitialHelper @Inject constructor(
    private val admobConsentHelper: AdmobConsentHelper,
    private val analyticsTracker: AnalyticsTracker,
    private val loadingAdsDialog: LoadingAdsDialog
) {
    private fun getAdRequest(timeout: Int = 60000): AdRequest {
        return AdRequest.Builder().setHttpTimeoutMillis(timeout).build()
    }

    fun showInterstitial(
        activity: Activity,
        interstitialAdUnitId: String,
        interstitialAd: InterstitialAd?,
        timeoutMilliSecond: Int?,
        adCallback: InterstitialAdCallback?
    ) {
        if (!admobConsentHelper.canRequestAds()) {
            adCallback?.onAdClosed()
            return
        }
        if (interstitialAd == null) {
            loadInterstitial(activity, interstitialAdUnitId, timeoutMilliSecond, object : InterstitialAdCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    showInterstitial(activity, interstitialAd, adCallback)
                }

                override fun onAdFailedToLoad(adError: LoadAdError?) {
                    adCallback?.onAdFailedToLoad()
                    adCallback?.onAdClosed()
                }
            })
        }
        else {
            showInterstitial(activity, interstitialAd, adCallback)
        }
    }

    fun showInterstitial(
        activity: Activity,
        interstitialAd: InterstitialAd,
        adCallback: InterstitialAdCallback?
    ) {
        analyticsTracker.trackEvent("aj_inters_show")
        interstitialAd.apply {
            onPaidEventListener = OnPaidEventListener { adValue ->
                analyticsTracker.trackRevenueEvent(
                    adValue,
                    interstitialAd.responseInfo.loadedAdapterResponseInfo?.adSourceName
                        ?: "AdMob",
                    "Interstitial"
                )
            }
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    adCallback?.onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    adCallback?.onAdClosed()
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    analyticsTracker.trackEvent("aj_inters_displayed")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    adCallback?.onAdClicked()
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    adCallback?.onAdImpression()
                }
            }
        }
        interstitialAd.show(activity)
    }

    fun loadInterstitial(
        activity: Activity,
        interstitialAdUnitId: String,
        timeoutMilliSecond: Int?,
        adCallback: InterstitialAdCallback?
    ) {
        if (!admobConsentHelper.canRequestAds()) {
            adCallback?.onAdFailedToLoad()
            return
        }

        analyticsTracker.trackEvent("aj_inters_load")

        if (loadingAdsDialog.isShowing) loadingAdsDialog.dismiss()
        loadingAdsDialog.show()
        val adUnitId = if (Constant.DEBUG_MODE) Constant.ADMOB_INTERSTITIAL_AD_UNIT_ID else interstitialAdUnitId
        InterstitialAd.load(activity, adUnitId, getAdRequest(timeoutMilliSecond ?: 60000), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                adCallback?.onAdLoaded(interstitialAd)
                loadingAdsDialog.dismiss()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                adCallback?.onAdFailedToLoad(adError)
                loadingAdsDialog.dismiss()
            }
        })
    }
}