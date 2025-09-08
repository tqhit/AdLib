package com.tqhit.adlib.sdk.ads.loader

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.tqhit.adlib.sdk.ads.AdmobHelper
import com.tqhit.adlib.sdk.ads.callback.BannerAdCallback
import com.tqhit.adlib.sdk.ads.callback.InterstitialAdCallback
import com.tqhit.adlib.sdk.ads.callback.NativeAdCallback
import com.tqhit.adlib.sdk.ads.callback.RewardAdCallback
import com.tqhit.adlib.sdk.firebase.FirebaseRemoteConfigHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityAdLoader @Inject constructor(
    private val remoteConfigHelper: FirebaseRemoteConfigHelper,
    private val admobHelper: AdmobHelper
) {
    private val loadedAdsLiveData = mutableMapOf<String, MutableLiveData<Any>>()
    private val adLoadInProgress = mutableSetOf<String>()

    companion object {
        private const val TAG = "ActivityAdLoader"

        // Prefix for preloaded ads
        const val PRELOAD_PREFIX = "PRELOAD_"

        // Suffixes for different ad types
        const val INTERSTITIAL_SUFFIX = "_IV"
        const val REWARDED_SUFFIX = "_RV"
        const val NATIVE_SUFFIX = "_NT"
        const val BANNER_SUFFIX = "_BN"
        const val COLLAPSIBLE_BANNER_SUFFIX = "_C_BN"
        const val AOA_SUFFIX = "_AOA"

        // Remote Config Keys for Ad Unit IDs
        const val RC_INTERSTITIAL_AD_UNIT_ID = "iv_ad_unit_id"
        const val RC_REWARDED_AD_UNIT_ID = "rv_ad_unit_id"
        const val RC_NATIVE_AD_UNIT_ID = "nt_ad_unit_id"
        const val RC_BANNER_AD_UNIT_ID = "bn_ad_unit_id"
        const val RC_C_BANNER_AD_UNIT_ID = "c_bn_ad_unit_id"
        const val RC_AOA_AD_UNIT_ID = "aoa_ad_unit_id"

        const val RC_ENABLE_ACTIVITY_AD_LOADER = "enable_activity_ad_loader"
    }

    fun onActivityResumed(activity: Activity, fragmentName: String? = null) {
        if (remoteConfigHelper.getBoolean(RC_ENABLE_ACTIVITY_AD_LOADER).not()) {
            Log.d(TAG, "ActivityAdLoader is disabled via remote config.")
            return
        }

        val activityKeyBase = activity.javaClass.simpleName + (fragmentName?.let { "_$it" } ?: "")
        val remoteConfigKeyForActivity = "${PRELOAD_PREFIX}${activityKeyBase}" // e.g., PRELOAD_MainActivity

        Log.d(TAG, "onActivityResumed for: $activityKeyBase")

        val adIdentifiersString = remoteConfigHelper.getString(remoteConfigKeyForActivity)

        if (adIdentifiersString.isBlank()) {
            Log.d(TAG, "No ad identifiers found in Remote Config for key: $remoteConfigKeyForActivity")
            return
        }

        Log.d(TAG, "Found ad identifiers for $remoteConfigKeyForActivity: \"$adIdentifiersString\"")
        val adIdentifiers = adIdentifiersString.split(',').map { it.trim() }.filter { it.isNotEmpty() }

        for (adKey in adIdentifiers) {
            preloadAdForKey(activity, adKey)
        }
    }

    private fun preloadAdForKey(activity: Activity, adKey: String) {
        if (loadedAdsLiveData.containsKey(adKey) || adLoadInProgress.contains(adKey)) {
            val status = if (loadedAdsLiveData.containsKey(adKey)) "already loaded/loading (has LiveData)" else "load in progress"
            Log.d(TAG, "Ad for $adKey $status. Skipping preload.")
            return
        }

        Log.d(TAG, "Attempting to preload ad for key: $adKey")
        adLoadInProgress.add(adKey)
        // Initialize LiveData immediately so observers can start observing
        val adLiveData = MutableLiveData<Any>()
        loadedAdsLiveData[adKey] = adLiveData

        var adUnitId: String? = null
        var adType: String? = null

        when {
            adKey.endsWith(INTERSTITIAL_SUFFIX) -> {
                adType = "Interstitial"
                adUnitId = remoteConfigHelper.getString(RC_INTERSTITIAL_AD_UNIT_ID)
                if (adUnitId.isNotEmpty()) {
                    admobHelper.loadInterstitial(activity, adUnitId, 10000, object : InterstitialAdCallback() {
                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            super.onAdLoaded(interstitialAd)
                            handleAdLoaded(adKey, interstitialAd, adType, adLiveData)
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError?) {
                            super.onAdFailedToLoad(adError)
                            handleAdFailedToLoad(adKey, adError?.message, adType)
                        }
                    })
                }
            }
            adKey.endsWith(REWARDED_SUFFIX) -> {
                adType = "Rewarded"
                adUnitId = remoteConfigHelper.getString(RC_REWARDED_AD_UNIT_ID)
                if (adUnitId.isNotEmpty()) {
                    admobHelper.loadReward(activity, adUnitId, 10000, object : RewardAdCallback() {
                        override fun onAdLoaded(rewardedAd: RewardedAd) {
                            super.onAdLoaded(rewardedAd)
                            handleAdLoaded(adKey, rewardedAd, adType, adLiveData)
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError?) {
                            super.onAdFailedToLoad(adError)
                            handleAdFailedToLoad(adKey, adError?.message, adType)
                        }
                    })
                }
            }
            adKey.endsWith(NATIVE_SUFFIX) -> {
                adType = "Native"
                adUnitId = remoteConfigHelper.getString(RC_NATIVE_AD_UNIT_ID)
                if (adUnitId.isNotEmpty()) {
                    admobHelper.loadNative(activity, adUnitId, 100000, object : NativeAdCallback() {
                        override fun onAdLoaded(nativeAd: NativeAd) {
                            super.onAdLoaded(nativeAd)
                            handleAdLoaded(adKey, nativeAd, adType, adLiveData)
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError?) {
                            super.onAdFailedToLoad(adError)
                            handleAdFailedToLoad(adKey, adError?.message, adType)
                        }
                    })
                }
            }
            adKey.endsWith(BANNER_SUFFIX) -> {
                adType = "Banner"
                adUnitId = remoteConfigHelper.getString(RC_BANNER_AD_UNIT_ID)
                if (adUnitId.isNotEmpty()) {
                    admobHelper.loadBanner(activity, adUnitId, 10000, object : BannerAdCallback() {
                        override fun onAdLoaded(adView: AdView) {
                            super.onAdLoaded(adView)
                            handleAdLoaded(adKey, adView, adType, adLiveData)
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError?) {
                            super.onAdFailedToLoad(adError)
                            handleAdFailedToLoad(adKey, adError?.message, adType)
                        }
                    })
                }
            }
            adKey.endsWith(COLLAPSIBLE_BANNER_SUFFIX) -> {
                adType = "Collapsible Banner"
                adUnitId = remoteConfigHelper.getString(RC_C_BANNER_AD_UNIT_ID)
                if (adUnitId.isNotEmpty()) {
                    admobHelper.loadCollapsibleBanner(activity, adUnitId, 10000, object : BannerAdCallback() {
                        override fun onAdLoaded(adView: AdView) {
                            super.onAdLoaded(adView)
                            handleAdLoaded(adKey, adView, adType, adLiveData)
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError?) {
                            super.onAdFailedToLoad(adError)
                            handleAdFailedToLoad(adKey, adError?.message, adType)
                        }
                    })
                }
            }
            adKey.endsWith(AOA_SUFFIX) -> {
                adType = "AppOpenAd"
                adUnitId = remoteConfigHelper.getString(RC_AOA_AD_UNIT_ID)
                if (adUnitId.isNotEmpty()) {
                    admobHelper.setAppOpenAdUnitId(adUnitId)
                    admobHelper.loadAOA(activity)
                }
            }
            else -> {
                Log.w(TAG, "Unknown ad type suffix for key: $adKey. Cannot determine ad type.")
                adLoadInProgress.remove(adKey)
                loadedAdsLiveData.remove(adKey) // Clean up placeholder LiveData
                return
            }
        }

        if (adUnitId.isEmpty()) {
            Log.w(TAG, "Ad Unit ID for $adType ($adKey) is null or empty in Remote Config. Cannot load ad.")
            adLoadInProgress.remove(adKey)
            loadedAdsLiveData.remove(adKey) // Clean up placeholder LiveData
        }
    }

    private fun handleAdLoaded(adKey: String, adObject: Any?, adType: String, liveDataToUpdate: MutableLiveData<Any>) {
        adLoadInProgress.remove(adKey)
        if (adObject != null) {
            Log.d(TAG, "$adType ad loaded successfully for key: $adKey")
            liveDataToUpdate.postValue(adObject) // Update the LiveData with the loaded ad
        } else {
            Log.e(TAG, "$adType ad loaded as null for key: $adKey")
            loadedAdsLiveData.remove(adKey) // Remove LiveData if ad is null
        }
    }

    private fun handleAdFailedToLoad(adKey: String, error: String?, adType: String) {
        adLoadInProgress.remove(adKey)
        Log.w(TAG, "Failed to load $adType ad for $adKey: $error")
        // Do not retry as per requirements
        loadedAdsLiveData.remove(adKey) // Remove the LiveData as the load failed
    }

    /**
     * Retrieves the MutableLiveData for a given ad key.
     * Consumers can observe this LiveData to get the ad when it's loaded.
     */
    fun getAdLiveData(key: String): MutableLiveData<Any>? {
        val liveData = loadedAdsLiveData[key]
        if (liveData == null) {
            Log.d(TAG, "No LiveData found in cache for key: $key. It might not have been requested or failed to load.")
        }
        return liveData
    }

    /**
     * Call this when an ad associated with a specific key has been shown and should be cleared.
     * This is important for one-time use ads like Interstitials.
     */
    fun adShownAndShouldBeRemoved(key: String) {
        val removedLiveData = loadedAdsLiveData.remove(key)
        if (removedLiveData != null) {
            Log.d(TAG, "Removed LiveData for ad key after it was shown: $key")
        }
    }
}