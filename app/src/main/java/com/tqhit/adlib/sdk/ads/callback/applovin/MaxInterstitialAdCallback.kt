package com.tqhit.adlib.sdk.ads.callback.applovin

import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd

open class MaxInterstitialAdCallback {
    open fun onAdClicked() {}

    open fun onAdClosed() {}

    open fun onAdFailedToLoad(error: MaxError? = null) {}

    open fun onAdFailedToShowFullScreenContent(error: MaxError? = null) {}

    open fun onAdImpression() {}

    open fun onAdLoaded(interstitialAd: MaxInterstitialAd) {}

    open fun onAdOpened() {}

    fun onAdSwipeGestureClicked() {}
}
