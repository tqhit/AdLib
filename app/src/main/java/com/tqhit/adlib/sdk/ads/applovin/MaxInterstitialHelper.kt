package com.tqhit.adlib.sdk.ads.applovin

import android.app.Activity
import android.content.Context
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxAdRevenueListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.tqhit.adlib.sdk.ads.AdFrequencyManager
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
    private val preferencesHelper: PreferencesHelper,
    private val adFrequencyManager: AdFrequencyManager
) : MaxAdListener, MaxAdRevenueListener {
    private val TAG = MaxInterstitialHelper::class.java.simpleName
    // Local variables to store callbacks
    private var currentLoadCallback: MaxInterstitialAdCallback? = null
    private var currentShowCallback: MaxInterstitialAdCallback? = null
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

        // Store the load callback
        currentLoadCallback = adCallback

        analyticsTracker.logEvent("aj_inters_load")
        val unitId = if (Constant.DEBUG_MODE) Constant.MAX_INTERSTITIAL_AD_UNIT_ID else adUnitId
        val interstitial = MaxInterstitialAd(unitId)
        interstitial.setListener(this)
        interstitial.setRevenueListener(this)
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
        
        // Check frequency and delay rules
        if (!adFrequencyManager.canShowInterstitial()) {
            adCallback?.onAdClosed()
            return
        }

        if (interstitial == null) {
            val loadingAdsDialog = LoadingAdsDialog(activity)
            if (!activity.isFinishing && !activity.isDestroyed)
                loadingAdsDialog.show()
            
            // Store the show callback before loading
            currentShowCallback = adCallback
            
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
            
            // Store the show callback
            currentShowCallback = adCallback
            
            interstitial.showAd(activity)
        } else {
            adCallback?.onAdClosed()
        }
    }

    // MaxAdListener implementation
    override fun onAdLoaded(ad: MaxAd) {
        analyticsTracker.logEvent("aj_inters_load_success")
        currentLoadCallback?.onAdLoaded(ad as MaxInterstitialAd)
        currentLoadCallback = null
    }

    override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
        analyticsTracker.logEvent("aj_inters_load_fail")
        currentLoadCallback?.onAdFailedToLoad(error)
        currentLoadCallback = null
    }

    override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
        analyticsTracker.logEvent("aj_inters_show_fail")
        currentShowCallback?.onAdFailedToShowFullScreenContent(error)
        currentLoadCallback = null
        currentShowCallback = null
    }

    override fun onAdDisplayed(ad: MaxAd) {
        analyticsTracker.logEvent("aj_inters_show_success")
        currentShowCallback?.onAdOpened()
    }

    override fun onAdHidden(ad: MaxAd) {
        adFrequencyManager.recordInterstitialShown()
        analyticsTracker.logEvent("aj_inters_close")
        currentShowCallback?.onAdClosed()
        currentLoadCallback = null
        currentShowCallback = null
    }

    override fun onAdClicked(ad: MaxAd) {
        analyticsTracker.logEvent("aj_inters_click")
        currentShowCallback?.onAdClicked()
    }

    // MaxAdRevenueListener implementation
    override fun onAdRevenuePaid(ad: MaxAd) {
        analyticsTracker.trackMaxRevenueEvent(ad)
    }
}