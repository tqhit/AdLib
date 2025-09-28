package com.tqhit.adlib.sdk.ads.callback.applovin

import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.ads.MaxRewardedAd

open class MaxRewardAdCallback {
    open fun onAdClicked() {}

    open fun onAdClosed() {}

    open fun onAdFailedToLoad(error: MaxError? = null) {}

    open fun onAdFailedToShowFullScreenContent(error: MaxError? = null) {}

    open fun onAdImpression() {}

    open fun onAdLoaded(rewardedAd: MaxRewardedAd) {}

    open fun onAdOpened() {}

    open fun onUserEarnedReward(reward: MaxReward?) {}

    fun onAdSwipeGestureClicked() {}
}
