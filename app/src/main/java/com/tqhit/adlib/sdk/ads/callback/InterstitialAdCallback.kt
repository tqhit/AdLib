package com.tqhit.adlib.sdk.ads.callback

import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd

open class InterstitialAdCallback {
    open fun onAdClicked() {}

    open fun onAdClosed() {}

    open fun onAdFailedToLoad(adError: LoadAdError? = null) {}

    open fun onAdImpression() {}

    open fun onAdLoaded(interstitialAd: InterstitialAd) {}

    open fun onAdOpened() {}

    fun onAdSwipeGestureClicked() {}
}