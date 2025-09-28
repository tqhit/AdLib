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
        onComplete: (() -> Unit)? = null
    ) {
        val sdk = AppLovinSdk.getInstance(context)
        val initConfigBuilder = AppLovinSdkInitializationConfiguration.builder(sdkKey ?: sdk.sdkKey)
            .setMediationProvider(AppLovinMediationProvider.MAX)
        if (!testDeviceIds.isNullOrEmpty()) {
            initConfigBuilder.setTestDeviceAdvertisingIds(testDeviceIds)
        }
        val initConfig = initConfigBuilder.build()
        sdk.initialize(initConfig) { _: AppLovinSdkConfiguration ->
            onComplete?.invoke()
        }
    }

    // Banner
    @MainThread
    fun loadBanner(activity: Activity, adUnitId: String, parent: ViewGroup?, listener: MaxBannerHelper.Listener?): MaxAdView? {
        return maxBannerHelper.loadBanner(activity, adUnitId, parent, listener)
    }

    // Interstitial
    fun loadInterstitial(context: Context, adUnitId: String, listener: MaxInterstitialHelper.Listener) {
        maxInterstitialHelper.load(context, adUnitId, listener)
    }

    fun showInterstitial(activity: Activity, adUnitId: String, interstitial: MaxInterstitialAd?, listener: MaxInterstitialHelper.Listener?) {
        maxInterstitialHelper.show(activity, adUnitId, interstitial, listener)
    }

    fun showInterstitial(activity: Activity, interstitial: MaxInterstitialAd, listener: MaxInterstitialHelper.Listener?) {
        maxInterstitialHelper.show(activity, interstitial, listener)
    }

    // Rewarded
    fun loadRewarded(context: Context, adUnitId: String, listener: MaxRewardedHelper.Listener) {
        maxRewardedHelper.load(context, adUnitId, listener)
    }

    fun showRewarded(activity: Activity, adUnitId: String, ad: MaxRewardedAd?, listener: MaxRewardedHelper.Listener?) {
        maxRewardedHelper.show(activity, adUnitId, ad, listener)
    }

    fun showRewarded(activity: Activity, ad: MaxRewardedAd, listener: MaxRewardedHelper.Listener?) {
        maxRewardedHelper.show(activity, ad, listener)
    }

    // Native
    fun loadNative(context: Context, adUnitId: String, listener: MaxNativeHelper.Listener) {
        maxNativeHelper.load(context, adUnitId, listener)
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
        maxNativeHelper.render(nativeAd, targetView, loader)
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


