package com.tqhit.adlib.sdk.ads.callback.applovin

import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxAd
import com.applovin.mediation.nativeAds.MaxNativeAdLoader

open class MaxNativeAdCallback {
    open fun onAdClicked() {}

    open fun onAdClosed() {}

    open fun onAdFailedToLoad(error: MaxError? = null) {}

    open fun onAdImpression() {}

    open fun onAdLoaded(nativeAd: MaxAd, loader: MaxNativeAdLoader) {}

    open fun onAdOpened() {}

    fun onAdSwipeGestureClicked() {}
}
