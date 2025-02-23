package com.tqhit.adlib.sdk.ads.callback

import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd

open class NativeAdCallback {
    open fun onAdClicked() {}

    open fun onAdClosed() {}

    open fun onAdFailedToLoad(adError: LoadAdError? = null) {}

    open fun onAdImpression() {}

    open fun onAdLoaded(nativeAd: NativeAd) {}

    open fun onAdOpened() {}

    fun onAdSwipeGestureClicked() {}
}