package com.tqhit.adlib.sdk.ads.callback.applovin

import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView

open class MaxBannerAdCallback {
    open fun onAdClicked() {}

    open fun onAdClosed() {}

    open fun onAdFailedToLoad(error: MaxError? = null) {}

    open fun onAdImpression() {}

    open fun onAdLoaded(adView: MaxAdView) {}

    open fun onAdOpened() {}

    fun onAdSwipeGestureClicked() {}
}
