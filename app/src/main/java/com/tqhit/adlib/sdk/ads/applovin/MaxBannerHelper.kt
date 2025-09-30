package com.tqhit.adlib.sdk.ads.applovin

import android.app.Activity
import android.view.ViewGroup
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.MaxAdFormat
import com.tqhit.adlib.sdk.ads.callback.applovin.MaxBannerAdCallback
import com.tqhit.adlib.sdk.analytics.AnalyticsTracker
import com.tqhit.adlib.sdk.data.local.PreferencesHelper
import com.tqhit.adlib.sdk.firebase.FirebaseRemoteConfigHelper
import com.tqhit.adlib.sdk.utils.Constant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaxBannerHelper @Inject constructor(
    private val analyticsTracker: AnalyticsTracker,
    private val remoteConfigHelper: FirebaseRemoteConfigHelper,
    private val preferencesHelper: PreferencesHelper
) {
    private fun isAdEnabled() =
        remoteConfigHelper.getBoolean("bn_enable")
                && !preferencesHelper.getBoolean(Constant.IS_PREMIUM, false)

    fun loadBanner(
        activity: Activity,
        adUnitId: String,
        parent: ViewGroup?,
        adCallback: MaxBannerAdCallback?
    ): MaxAdView? {
        if (!isAdEnabled()) {
            adCallback?.onAdFailedToLoad(null)
            return null
        }
        analyticsTracker.logEvent("aj_banner_load")
        val unitId = if (Constant.DEBUG_MODE) Constant.MAX_BANNER_AD_UNIT_ID else adUnitId
        val adView = MaxAdView(unitId, MaxAdFormat.BANNER)
        adView.setListener(object : MaxAdViewAdListener {
            override fun onAdLoaded(ad: MaxAd) {
                analyticsTracker.logEvent("aj_banner_load_success")
                adCallback?.onAdLoaded(adView)
            }
            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                analyticsTracker.logEvent("aj_banner_load_fail")
                adCallback?.onAdFailedToLoad(error)
            }
            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                analyticsTracker.logEvent("aj_banner_show_fail")
            }
            override fun onAdDisplayed(ad: MaxAd) {
                analyticsTracker.logEvent("aj_banner_show_success")
                adCallback?.onAdOpened()
            }
            override fun onAdHidden(ad: MaxAd) {
                analyticsTracker.logEvent("aj_banner_close")
                adCallback?.onAdClosed()
            }
            override fun onAdClicked(ad: MaxAd) {
                analyticsTracker.logEvent("aj_banner_click")
                adCallback?.onAdClicked()
            }
            override fun onAdExpanded(ad: MaxAd) {}
            override fun onAdCollapsed(ad: MaxAd) {}
        })
        adView.setRevenueListener { ad ->
            analyticsTracker.trackMaxRevenueEvent(ad)
        }
        parent?.let {
            it.removeAllViews()
            it.addView(adView)
        }
        adView.loadAd()
        return adView
    }

}


