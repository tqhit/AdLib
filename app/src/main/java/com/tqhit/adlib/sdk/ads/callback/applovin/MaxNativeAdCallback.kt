package com.tqhit.adlib.sdk.ads.callback.applovin

import com.applovin.mediation.MaxError
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView

open class MaxNativeAdCallback {
    open fun onAdClicked() {}

    open fun onAdClosed() {}

    open fun onAdFailedToLoad(error: MaxError? = null) {}

    open fun onAdImpression() {}

    open fun onAdLoaded(nativeAdView: MaxNativeAdView, loader: MaxNativeAdLoader) {}

    open fun onAdOpened() {}

    fun onAdSwipeGestureClicked() {}
}
