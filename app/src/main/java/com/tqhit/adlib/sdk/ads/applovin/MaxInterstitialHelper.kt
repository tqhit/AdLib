package com.tqhit.adlib.sdk.ads.applovin

import android.app.Activity
import android.content.Context
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.tqhit.adlib.sdk.ads.callback.applovin.MaxInterstitialAdCallback
import com.tqhit.adlib.sdk.analytics.AnalyticsTracker
import com.tqhit.adlib.sdk.data.local.PreferencesHelper
import com.tqhit.adlib.sdk.firebase.FirebaseRemoteConfigHelper
import com.tqhit.adlib.sdk.ui.dialog.LoadingAdsDialog
import com.tqhit.adlib.sdk.utils.Constant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaxInterstitialHelper @Inject constructor(
    private val analyticsTracker: AnalyticsTracker,
    private val remoteConfigHelper: FirebaseRemoteConfigHelper,
    private val preferencesHelper: PreferencesHelper
) {
    private val enableAd by lazy {
        remoteConfigHelper.getBoolean("iv_enable")
                && !preferencesHelper.getBoolean(Constant.IS_PREMIUM, false)
    }

    fun loadInterstitial(
        context: Context,
        adUnitId: String,
        adCallback: MaxInterstitialAdCallback?
    ) {
        if (!enableAd) {
            adCallback?.onAdFailedToLoad(null)
            return
        }

        analyticsTracker.logEvent("aj_inters_load")
        val unitId = if (Constant.DEBUG_MODE) Constant.MAX_INTERSTITIAL_AD_UNIT_ID else adUnitId
        val interstitial = MaxInterstitialAd(unitId)
        interstitial.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd) {
                analyticsTracker.logEvent("aj_inters_load_success")
                adCallback?.onAdLoaded(interstitial)
            }

            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                analyticsTracker.logEvent("aj_inters_load_fail")
                adCallback?.onAdFailedToLoad(error)
            }

            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                analyticsTracker.logEvent("aj_inters_show_fail")
                adCallback?.onAdFailedToShowFullScreenContent(error)
            }

            override fun onAdDisplayed(ad: MaxAd) {
                analyticsTracker.logEvent("aj_inters_show_success")
                adCallback?.onAdOpened()
            }

            override fun onAdHidden(ad: MaxAd) {
                analyticsTracker.logEvent("aj_inters_close")
                adCallback?.onAdClosed()
            }

            override fun onAdClicked(ad: MaxAd) {
                analyticsTracker.logEvent("aj_inters_click")
                adCallback?.onAdClicked()
            }
        })
        interstitial.setRevenueListener { ad ->
            analyticsTracker.trackMaxRevenueEvent(ad)
        }
        interstitial.loadAd()
    }

    fun showInterstitial(
        activity: Activity,
        interstitialAdUnitId: String,
        interstitial: MaxInterstitialAd?,
        adCallback: MaxInterstitialAdCallback?
    ) {
        if (!enableAd) {
            adCallback?.onAdClosed()
            return
        }

        if (interstitial == null) {
            val loadingAdsDialog = LoadingAdsDialog(activity)
            if (!activity.isFinishing && !activity.isDestroyed)
                loadingAdsDialog.show()
            loadInterstitial(activity, interstitialAdUnitId, object : MaxInterstitialAdCallback() {
                override fun onAdLoaded(interstitialAd: MaxInterstitialAd) {
                    showInterstitial(activity, interstitialAd, adCallback)
                    if (loadingAdsDialog.isShowing) {
                        loadingAdsDialog.dismiss()
                    }
                }

                override fun onAdFailedToLoad(error: MaxError?) {
                    adCallback?.onAdFailedToLoad(error)
                    adCallback?.onAdClosed()
                    if (loadingAdsDialog.isShowing) {
                        loadingAdsDialog.dismiss()
                    }
                }
            })
        } else {
            showInterstitial(activity, interstitial, adCallback)
        }
    }

    fun showInterstitial(
        activity: Activity,
        interstitial: MaxInterstitialAd,
        adCallback: MaxInterstitialAdCallback?
    ) {
        if (interstitial.isReady) {
            analyticsTracker.logEvent("aj_inters_show")
            interstitial.showAd(activity)
        } else {
            adCallback?.onAdClosed()
        }
    }
}


