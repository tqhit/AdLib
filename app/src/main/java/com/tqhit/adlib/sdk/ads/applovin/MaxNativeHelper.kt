package com.tqhit.adlib.sdk.ads.applovin

import android.content.Context
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import com.tqhit.adlib.sdk.ads.callback.applovin.MaxNativeAdCallback
import com.tqhit.adlib.sdk.analytics.AnalyticsTracker
import com.tqhit.adlib.sdk.data.local.PreferencesHelper
import com.tqhit.adlib.sdk.firebase.FirebaseRemoteConfigHelper
import com.tqhit.adlib.sdk.utils.Constant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaxNativeHelper @Inject constructor(
    private val analyticsTracker: AnalyticsTracker,
    private val remoteConfigHelper: FirebaseRemoteConfigHelper,
    private val preferencesHelper: PreferencesHelper
) {
    private val enableAd by lazy {
        remoteConfigHelper.getBoolean("nt_enable")
                && !preferencesHelper.getBoolean(Constant.IS_PREMIUM, false)
    }

    fun loadNative(
        context: Context,
        adUnitId: String,
        adCallback: MaxNativeAdCallback?
    ) {
        if (!enableAd) {
            adCallback?.onAdFailedToLoad(null)
            return
        }
        analyticsTracker.logEvent("aj_native_load")
        val unitId = if (Constant.DEBUG_MODE) Constant.MAX_NATIVE_AD_UNIT_ID else adUnitId
        val loader = MaxNativeAdLoader(unitId)
        loader.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: com.applovin.mediation.MaxAd) {
                analyticsTracker.logEvent("aj_native_load_success")
                if (nativeAdView != null) {
                    adCallback?.onAdLoaded(nativeAdView, loader)
                } else {
                    adCallback?.onAdFailedToLoad(null)
                }
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                analyticsTracker.logEvent("aj_native_load_fail")
                adCallback?.onAdFailedToLoad(error)
            }

            override fun onNativeAdClicked(ad: com.applovin.mediation.MaxAd) {
                analyticsTracker.logEvent("aj_native_click")
                adCallback?.onAdClicked()
            }
        })
        loader.setRevenueListener { ad ->
            analyticsTracker.trackMaxRevenueEvent(ad)
        }
        loader.loadAd()
    }

    fun show(nativeAdView: MaxNativeAdView, parent: android.view.ViewGroup) {
        parent.removeAllViews()
        parent.addView(nativeAdView)
    }

    fun buildNativeAdBinder(
        layoutResId: Int,
        titleTextViewId: Int,
        bodyTextViewId: Int,
        starRatingContainerId: Int,
        advertiserTextViewId: Int,
        iconImageViewId: Int,
        mediaContainerId: Int,
        optionsContainerId: Int,
        ctaButtonId: Int
    ): MaxNativeAdViewBinder {
        return MaxNativeAdViewBinder.Builder(layoutResId)
            .setTitleTextViewId(titleTextViewId)
            .setBodyTextViewId(bodyTextViewId)
            .setStarRatingContentViewGroupId(starRatingContainerId)
            .setAdvertiserTextViewId(advertiserTextViewId)
            .setIconImageViewId(iconImageViewId)
            .setMediaContentViewGroupId(mediaContainerId)
            .setOptionsContentViewGroupId(optionsContainerId)
            .setCallToActionButtonId(ctaButtonId)
            .build()
    }

    fun createNativeAdView(context: Context, binder: MaxNativeAdViewBinder): MaxNativeAdView {
        return MaxNativeAdView(binder, context)
    }

    fun renderNative(nativeAd: MaxAd, targetView: MaxNativeAdView, loader: MaxNativeAdLoader) {
        loader.render(targetView, nativeAd)
    }
}


