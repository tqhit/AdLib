package com.tqhit.adlib.sdk.ads.applovin

import android.app.Activity
import android.content.Context
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxRewardedAd
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

    fun load(
        context: Context,
        adUnitId: String,
        listener: Listener
    ) {
        if (!enableAd) {
            listener.onFailed(null)
            return
        }
        analyticsTracker.logEvent("aj_reward_load")
        val unitId = if (Constant.DEBUG_MODE) Constant.MAX_REWARDED_AD_UNIT_ID else adUnitId
        val rewarded = MaxRewardedAd.getInstance(unitId)
        rewarded.setListener(object : MaxRewardedAdListener {
            override fun onAdLoaded(ad: MaxAd) {
                analyticsTracker.logEvent("aj_reward_load_success")
                listener.onLoaded(rewarded)
            }
            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                analyticsTracker.logEvent("aj_reward_load_fail")
                listener.onFailed(error)
            }
            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                analyticsTracker.logEvent("aj_reward_show_fail")
                listener.onClosed()
            }
            override fun onAdDisplayed(ad: MaxAd) {
                analyticsTracker.logEvent("aj_reward_show_success")
            }
            override fun onAdHidden(ad: MaxAd) {
                analyticsTracker.logEvent("aj_reward_close")
                listener.onClosed()
            }
            override fun onAdClicked(ad: MaxAd) {
                analyticsTracker.logEvent("aj_reward_click")
                listener.onClicked()
            }
            override fun onUserRewarded(ad: MaxAd, reward: MaxReward) {
                listener.onReward(reward)
            }
        })
        rewarded.setRevenueListener { ad ->
            analyticsTracker.trackMaxRevenueEvent(ad)
        }
        rewarded.loadAd()
    }

    fun show(
        activity: Activity,
        rewardedAdUnitId: String,
        rewardedAd: MaxRewardedAd?,
        listener: Listener?
    ) {
        if (!enableAd) {
            listener?.onFailed(null)
            return
        }

        if (rewardedAd == null) {
            val loadingAdsDialog = LoadingAdsDialog(activity)
            if (!activity.isFinishing && !activity.isDestroyed)
                loadingAdsDialog.show()
            load(activity, rewardedAdUnitId, object : Listener {
                override fun onLoaded(ad: MaxRewardedAd) {
                    show(activity, ad, listener)
                    if (loadingAdsDialog.isShowing) {
                        loadingAdsDialog.dismiss()
                    }
                }

                override fun onFailed(error: Any?) {
                    listener?.onFailed(error)
                    if (loadingAdsDialog.isShowing) {
                        loadingAdsDialog.dismiss()
                    }
                }

                override fun onClosed() { }
                override fun onClicked() { }
                override fun onReward(reward: MaxReward) { }
            })
        } else {
            show(activity, rewardedAd, listener)
        }
    }

    fun show(activity: Activity, ad: MaxRewardedAd, listener: Listener?) {
        if (ad.isReady) {
            analyticsTracker.logEvent("aj_reward_show")
            ad.showAd(activity)
        } else {
            listener?.onClosed()
        }
    }

    interface Listener {
        fun onLoaded(ad: MaxRewardedAd)
        fun onFailed(error: Any?)
        fun onClosed()
        fun onClicked()
        fun onReward(reward: MaxReward)
    }
}


