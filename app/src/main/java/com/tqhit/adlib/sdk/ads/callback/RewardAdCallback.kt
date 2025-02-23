package com.tqhit.adlib.sdk.ads.callback

import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd

open class RewardAdCallback {
    open fun onAdClicked() {}

    open fun onAdClosed() {}

    open fun onAdFailedToLoad(adError: LoadAdError? = null) {}

    open fun onAdImpression() {}

    open fun onAdLoaded(rewardedAd: RewardedAd) {}

    open fun onUserEarnedReward(rewardItem: RewardItem?) {}

    open fun onAdOpened() {}

    fun onAdSwipeGestureClicked() {}
}