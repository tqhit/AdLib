package com.tqhit.adlib.sdk.ads.applovin

import android.app.Activity
import android.content.Context
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxRewardedAd
import com.tqhit.adlib.sdk.ads.callback.applovin.MaxRewardAdCallback
import com.tqhit.adlib.sdk.analytics.AnalyticsTracker
import com.tqhit.adlib.sdk.data.local.PreferencesHelper
import com.tqhit.adlib.sdk.firebase.FirebaseRemoteConfigHelper
import com.tqhit.adlib.sdk.ui.dialog.LoadingAdsDialog
import com.tqhit.adlib.sdk.utils.Constant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaxRewardedHelper @Inject constructor(
    private val analyticsTracker: AnalyticsTracker,
    private val remoteConfigHelper: FirebaseRemoteConfigHelper,
    private val preferencesHelper: PreferencesHelper
) {
    private val enableAd by lazy {
        remoteConfigHelper.getBoolean("rv_enable")
                && !preferencesHelper.getBoolean(Constant.IS_PREMIUM, false)
    }

    fun loadReward(
        context: Context,
        adUnitId: String,
        adCallback: MaxRewardAdCallback?
    ) {
        if (!enableAd) {
            adCallback?.onAdFailedToLoad(null)
            return
        }
        analyticsTracker.logEvent("aj_reward_load")
        val unitId = if (Constant.DEBUG_MODE) Constant.MAX_REWARDED_AD_UNIT_ID else adUnitId
        val rewarded = MaxRewardedAd.getInstance(unitId)
        rewarded.setListener(object : MaxRewardedAdListener {
            override fun onAdLoaded(ad: MaxAd) {
                analyticsTracker.logEvent("aj_reward_load_success")
                adCallback?.onAdLoaded(rewarded)
            }
            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                analyticsTracker.logEvent("aj_reward_load_fail")
                adCallback?.onAdFailedToLoad(error)
            }
            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                analyticsTracker.logEvent("aj_reward_show_fail")
                adCallback?.onAdFailedToShowFullScreenContent(error)
            }
            override fun onAdDisplayed(ad: MaxAd) {
                analyticsTracker.logEvent("aj_reward_show_success")
                adCallback?.onAdOpened()
            }
            override fun onAdHidden(ad: MaxAd) {
                analyticsTracker.logEvent("aj_reward_close")
                adCallback?.onAdClosed()
            }
            override fun onAdClicked(ad: MaxAd) {
                analyticsTracker.logEvent("aj_reward_click")
                adCallback?.onAdClicked()
            }
            override fun onUserRewarded(ad: MaxAd, reward: MaxReward) {
                adCallback?.onUserEarnedReward(reward)
            }
        })
        rewarded.setRevenueListener { ad ->
            analyticsTracker.trackMaxRevenueEvent(ad)
        }
        rewarded.loadAd()
    }

    fun showReward(
        activity: Activity,
        rewardedAdUnitId: String,
        rewardedAd: MaxRewardedAd?,
        adCallback: MaxRewardAdCallback?
    ) {
        if (!enableAd) {
            adCallback?.onAdFailedToLoad(null)
            return
        }

        if (rewardedAd == null) {
            val loadingAdsDialog = LoadingAdsDialog(activity)
            if (!activity.isFinishing && !activity.isDestroyed)
                loadingAdsDialog.show()
            loadReward(activity, rewardedAdUnitId, object : MaxRewardAdCallback() {
                override fun onAdLoaded(rewardedAd: MaxRewardedAd) {
                    showReward(activity, rewardedAd, adCallback)
                    if (loadingAdsDialog.isShowing) {
                        loadingAdsDialog.dismiss()
                    }
                }

                override fun onAdFailedToLoad(error: MaxError?) {
                    adCallback?.onAdFailedToLoad(error)
                    if (loadingAdsDialog.isShowing) {
                        loadingAdsDialog.dismiss()
                    }
                }
            })
        } else {
            showReward(activity, rewardedAd, adCallback)
        }
    }

    fun showReward(activity: Activity, ad: MaxRewardedAd, adCallback: MaxRewardAdCallback?) {
        if (ad.isReady) {
            analyticsTracker.logEvent("aj_reward_show")
            ad.showAd(activity)
        } else {
            adCallback?.onAdClosed()
        }
    }
}


