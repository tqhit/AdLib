package com.tqhit.adlib.sdk.ads.applovin

import android.app.Activity
import android.content.Context
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
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

    fun load(
        context: Context,
        adUnitId: String,
        listener: Listener
    ) {
        if (!enableAd) {
            listener.onFailed(null)
            return
        }

        analyticsTracker.logEvent("aj_inters_load")
        val unitId = if (Constant.DEBUG_MODE) Constant.MAX_INTERSTITIAL_AD_UNIT_ID else adUnitId
        val interstitial = MaxInterstitialAd(unitId)
        interstitial.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd) {
                analyticsTracker.logEvent("aj_inters_load_success")
                listener.onLoaded(interstitial)
            }

            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                analyticsTracker.logEvent("aj_inters_load_fail")
                listener.onFailed(error)
            }

            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                analyticsTracker.logEvent("aj_inters_show_fail")
                listener.onClosed()
            }

            override fun onAdDisplayed(ad: MaxAd) {
                analyticsTracker.logEvent("aj_inters_show_success")
            }

            override fun onAdHidden(ad: MaxAd) {
                analyticsTracker.logEvent("aj_inters_close")
                listener.onClosed()
            }

            override fun onAdClicked(ad: MaxAd) {
                analyticsTracker.logEvent("aj_inters_click")
            }
        })
        interstitial.setRevenueListener { ad ->
            analyticsTracker.trackMaxRevenueEvent(ad)
        }
        interstitial.loadAd()
    }

    fun show(
        activity: Activity,
        interstitialAdUnitId: String,
        interstitial: MaxInterstitialAd?,
        listener: Listener?
    ) {
        if (!enableAd) {
            listener?.onClosed()
            return
        }

        if (interstitial == null) {
            val loadingAdsDialog = LoadingAdsDialog(activity)
            if (!activity.isFinishing && !activity.isDestroyed)
                loadingAdsDialog.show()
            load(activity, interstitialAdUnitId, object : Listener {
                override fun onLoaded(interstitial: MaxInterstitialAd) {
                    show(activity, interstitial, listener)
                    if (loadingAdsDialog.isShowing) {
                        loadingAdsDialog.dismiss()
                    }
                }

                override fun onFailed(error: Any?) {
                    listener?.onFailed(error)
                    listener?.onClosed()
                    if (loadingAdsDialog.isShowing) {
                        loadingAdsDialog.dismiss()
                    }
                }

                override fun onClosed() { }
            })
        } else {
            show(activity, interstitial, listener)
        }
    }

    fun show(
        activity: Activity,
        interstitial: MaxInterstitialAd,
        listener: Listener?
    ) {
        if (interstitial.isReady) {
            analyticsTracker.logEvent("aj_inters_show")
            interstitial.showAd(activity)
        } else {
            listener?.onClosed()
        }
    }

    interface Listener {
        fun onLoaded(interstitial: MaxInterstitialAd)
        fun onFailed(error: Any?)
        fun onClosed()
    }
}


