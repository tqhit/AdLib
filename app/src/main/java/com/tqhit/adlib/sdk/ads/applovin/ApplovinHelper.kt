package com.tqhit.adlib.sdk.ads.applovin

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.annotation.MainThread
import com.applovin.mediation.MaxAd
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import com.tqhit.adlib.sdk.ads.callback.applovin.MaxBannerAdCallback
import com.tqhit.adlib.sdk.ads.callback.applovin.MaxInterstitialAdCallback
import com.tqhit.adlib.sdk.ads.callback.applovin.MaxRewardAdCallback
import com.tqhit.adlib.sdk.ads.callback.applovin.MaxNativeAdCallback
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplovinHelper @Inject constructor(
    private val maxBannerHelper: MaxBannerHelper,
    private val maxInterstitialHelper: MaxInterstitialHelper,
    private val maxRewardedHelper: MaxRewardedHelper,
    private val maxNativeHelper: MaxNativeHelper,
    private val maxAppOpenHelper: MaxAppOpenHelper
) {
    fun initApplovin(
        context: Context,
        sdkKey: String? = null,
        testDeviceIds: List<String>? = null,
        onComplete: ((AppLovinSdkConfiguration) -> Unit)? = null
    ) {
        val sdk = AppLovinSdk.getInstance(context)
        val initConfigBuilder = AppLovinSdkInitializationConfiguration.builder(sdkKey ?: sdk.sdkKey)
            .setMediationProvider(AppLovinMediationProvider.MAX)
        if (!testDeviceIds.isNullOrEmpty()) {
            initConfigBuilder.setTestDeviceAdvertisingIds(testDeviceIds)
        }
        val initConfig = initConfigBuilder.build()
        sdk.initialize(initConfig) { sdkConfig: AppLovinSdkConfiguration ->
            onComplete?.invoke(sdkConfig)
        }
    }

    // Banner
    @MainThread
    fun loadBanner(activity: Activity, adUnitId: String, parent: ViewGroup?, adCallback: MaxBannerAdCallback?): MaxAdView? {
        return maxBannerHelper.loadBanner(activity, adUnitId, parent, adCallback)
    }

    // Interstitial
    fun loadInterstitial(context: Context, adUnitId: String, adCallback: MaxInterstitialAdCallback?) {
        maxInterstitialHelper.loadInterstitial(context, adUnitId, adCallback)
    }

    fun showInterstitial(activity: Activity, adUnitId: String, interstitial: MaxInterstitialAd?, adCallback: MaxInterstitialAdCallback?) {
        maxInterstitialHelper.showInterstitial(activity, adUnitId, interstitial, adCallback)
    }

    fun showInterstitial(activity: Activity, interstitial: MaxInterstitialAd, adCallback: MaxInterstitialAdCallback?) {
        maxInterstitialHelper.showInterstitial(activity, interstitial, adCallback)
    }

    // Rewarded
    fun loadReward(context: Context, adUnitId: String, adCallback: MaxRewardAdCallback?) {
        maxRewardedHelper.loadReward(context, adUnitId, adCallback)
    }

    fun showReward(activity: Activity, adUnitId: String, ad: MaxRewardedAd?, adCallback: MaxRewardAdCallback?) {
        maxRewardedHelper.showReward(activity, adUnitId, ad, adCallback)
    }

    fun showReward(activity: Activity, ad: MaxRewardedAd, adCallback: MaxRewardAdCallback?) {
        maxRewardedHelper.showReward(activity, ad, adCallback)
    }

    // Native
    fun loadNative(context: Context, adUnitId: String, adCallback: MaxNativeAdCallback?) {
        maxNativeHelper.loadNative(context, adUnitId, adCallback)
    }
    fun showNative(nativeAdView: MaxNativeAdView, parent: ViewGroup) {
        maxNativeHelper.show(nativeAdView, parent)
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
        return maxNativeHelper.buildNativeAdBinder(
            layoutResId,
            titleTextViewId,
            bodyTextViewId,
            starRatingContainerId,
            advertiserTextViewId,
            iconImageViewId,
            mediaContainerId,
            optionsContainerId,
            ctaButtonId
        )
    }

    fun createNativeAdView(context: Context, binder: MaxNativeAdViewBinder): MaxNativeAdView {
        return maxNativeHelper.createNativeAdView(context, binder)
    }

    fun renderNative(nativeAd: MaxAd, targetView: MaxNativeAdView, loader: MaxNativeAdLoader) {
        maxNativeHelper.renderNative(nativeAd, targetView, loader)
    }

    // App Open
    fun setAppOpenAdUnitId(adUnitId: String) { maxAppOpenHelper.setAdUnitId(adUnitId) }
    fun loadAOA(context: Context) { maxAppOpenHelper.loadAd(context) }
    fun showAOA(activity: Activity, listener: MaxAppOpenHelper.OnShowAdCompleteListener? = null) {
        maxAppOpenHelper.showAdIfAvailable(activity, listener ?: object : MaxAppOpenHelper.OnShowAdCompleteListener {
            override fun onShowAdComplete() {}
        })
    }
}


