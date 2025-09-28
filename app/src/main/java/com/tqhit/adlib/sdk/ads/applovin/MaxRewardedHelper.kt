package com.tqhit.adlib.sdk.ads.applovin

import android.app.Activity
import android.content.Context
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdRevenueListener
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
) : MaxRewardedAdListener, MaxAdRevenueListener {
    // Local variables to store callbacks
    private var currentLoadCallback: MaxRewardAdCallback? = null
    private var currentShowCallback: MaxRewardAdCallback? = null
    
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
        
        // Store the load callback
        currentLoadCallback = adCallback
        
        analyticsTracker.logEvent("aj_reward_load")
        val unitId = if (Constant.DEBUG_MODE) Constant.MAX_REWARDED_AD_UNIT_ID else adUnitId
        val rewarded = MaxRewardedAd.getInstance(unitId)
        rewarded.setListener(this)
        rewarded.setRevenueListener(this)
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
            
            // Store the show callback before loading
            currentShowCallback = adCallback
            
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
            
            // Store the show callback
            currentShowCallback = adCallback
            
            ad.showAd(activity)
        } else {
            adCallback?.onAdClosed()
        }
    }

    // MaxRewardedAdListener implementation
    override fun onAdLoaded(ad: MaxAd) {
        analyticsTracker.logEvent("aj_reward_load_success")
        currentLoadCallback?.onAdLoaded(ad as MaxRewardedAd)
        currentLoadCallback = null
    }

    override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
        analyticsTracker.logEvent("aj_reward_load_fail")
        currentLoadCallback?.onAdFailedToLoad(error)
        currentLoadCallback = null
    }

    override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
        analyticsTracker.logEvent("aj_reward_show_fail")
        currentShowCallback?.onAdFailedToShowFullScreenContent(error)
        currentLoadCallback = null
        currentShowCallback = null
    }

    override fun onAdDisplayed(ad: MaxAd) {
        analyticsTracker.logEvent("aj_reward_show_success")
        currentShowCallback?.onAdOpened()
    }

    override fun onAdHidden(ad: MaxAd) {
        analyticsTracker.logEvent("aj_reward_close")
        currentShowCallback?.onAdClosed()
        currentLoadCallback = null
        currentShowCallback = null
    }

    override fun onAdClicked(ad: MaxAd) {
        analyticsTracker.logEvent("aj_reward_click")
        currentShowCallback?.onAdClicked()
    }

    override fun onUserRewarded(ad: MaxAd, reward: MaxReward) {
        analyticsTracker.logEvent("aj_reward_earned")
        currentShowCallback?.onUserEarnedReward(reward)
    }

    override fun onAdRevenuePaid(p0: MaxAd) {
        analyticsTracker.trackMaxRevenueEvent(p0)
    }
}


