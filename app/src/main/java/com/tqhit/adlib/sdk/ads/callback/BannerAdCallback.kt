package com.tqhit.adlib.sdk.ads.callback

import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

open class BannerAdCallback {
    open fun onAdClicked() {}

    open fun onAdClosed() {}

    open fun onAdFailedToLoad(adError: LoadAdError? = null) {}

    open fun onAdImpression() {}

    open fun onAdLoaded(adView: AdView) {}

    open fun onAdOpened() {}

    fun onAdSwipeGestureClicked() {}
}