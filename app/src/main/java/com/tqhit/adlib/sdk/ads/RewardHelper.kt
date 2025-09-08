package com.tqhit.adlib.sdk.ads

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.tqhit.adlib.sdk.ads.callback.RewardAdCallback
import com.tqhit.adlib.sdk.analytics.AnalyticsTracker
import com.tqhit.adlib.sdk.data.local.PreferencesHelper
import com.tqhit.adlib.sdk.firebase.FirebaseRemoteConfigHelper
import com.tqhit.adlib.sdk.ui.dialog.LoadingAdsDialog
import com.tqhit.adlib.sdk.utils.Constant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardHelper @Inject constructor(
    private val admobConsentHelper: AdmobConsentHelper,
    private val analyticsTracker: AnalyticsTracker,
    private val remoteConfigHelper: FirebaseRemoteConfigHelper,
    private val preferencesHelper: PreferencesHelper
) {
    private val enableAd by lazy {
        remoteConfigHelper.getBoolean("rv_enable")
                && !preferencesHelper.getBoolean(Constant.IS_PREMIUM, false)
    }

    private fun getAdRequest(timeout: Int = 60000): AdRequest {
        return AdRequest.Builder().setHttpTimeoutMillis(timeout).build()
    }

    fun showReward(
        activity: Activity,
        rewardAdUnitId: String,
        rewardedAd: RewardedAd?,
        timeOutMilliSecond: Int?,
        adCallback: RewardAdCallback?
    ) {
        if (!enableAd || !admobConsentHelper.canRequestAds()) {
            adCallback?.onAdFailedToLoad()
            return
        }

        analyticsTracker.logEvent("aj_reward_load")

        if (rewardedAd == null) {
            val loadingAdsDialog = LoadingAdsDialog(activity)
            if (!activity.isFinishing && !activity.isDestroyed)
                loadingAdsDialog.show()
            loadReward(activity, rewardAdUnitId, timeOutMilliSecond, object : RewardAdCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    showReward(activity, rewardedAd, adCallback)
                    if (loadingAdsDialog.isShowing) {
                        loadingAdsDialog.dismiss()
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError?) {
                    adCallback?.onAdFailedToLoad(adError)
                    if (loadingAdsDialog.isShowing) {
                        loadingAdsDialog.dismiss()
                    }
                }
            })
        }
        else {
            showReward(activity, rewardedAd, adCallback)
        }
    }

    fun showReward(
        activity: Activity,
        rewardedAd: RewardedAd,
        adCallback: RewardAdCallback?
    ) {
        analyticsTracker.logEvent("aj_reward_show")
        rewardedAd.apply {
            onPaidEventListener = OnPaidEventListener { adValue: AdValue ->
                analyticsTracker.trackRevenueEvent(
                    adValue,
                    rewardedAd.responseInfo.loadedAdapterResponseInfo?.adSourceName
                        ?: "AdMob",
                    "Reward"
                )
            }
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    adCallback?.onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(var0: AdError) {
                    super.onAdFailedToShowFullScreenContent(var0)
                    adCallback?.onAdFailedToShowFullScreenContent(var0)
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    analyticsTracker.logEvent("aj_reward_displayed")
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    adCallback?.onAdImpression()
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    adCallback?.onAdClicked()
                }
            }
        }
        rewardedAd.show(activity, { rewardItem ->
            adCallback?.onUserEarnedReward(rewardItem)
        })
    }

    fun loadReward(
        activity: Activity,
        rewardAdUnitId: String,
        timeOutMilliSecond: Int?,
        adCallback: RewardAdCallback?
    ) {
        if (!enableAd || !admobConsentHelper.canRequestAds()) {
            adCallback?.onAdFailedToLoad(null)
            return
        }

        analyticsTracker.logEvent("aj_reward_load")

        val adUnitId = if (Constant.DEBUG_MODE) Constant.ADMOB_REWARDED_AD_UNIT_ID else rewardAdUnitId
        RewardedAd.load(activity, adUnitId, getAdRequest(timeOutMilliSecond ?: 60000), object: RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                adCallback?.onAdFailedToLoad(adError)
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                adCallback?.onAdLoaded(rewardedAd)
            }
        })
    }
}