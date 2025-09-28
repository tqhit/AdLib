package com.tqhit.adlib.sdk.ads.loader

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxAppOpenAd
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.tqhit.adlib.sdk.ads.admob.AdmobHelper
import com.tqhit.adlib.sdk.ads.callback.admob.BannerAdCallback
import com.tqhit.adlib.sdk.ads.callback.admob.InterstitialAdCallback
import com.tqhit.adlib.sdk.ads.callback.admob.NativeAdCallback
import com.tqhit.adlib.sdk.ads.callback.admob.RewardAdCallback
import com.tqhit.adlib.sdk.ads.callback.applovin.MaxBannerAdCallback
import com.tqhit.adlib.sdk.ads.callback.applovin.MaxInterstitialAdCallback
import com.tqhit.adlib.sdk.ads.callback.applovin.MaxNativeAdCallback
import com.tqhit.adlib.sdk.ads.callback.applovin.MaxRewardAdCallback
import com.tqhit.adlib.sdk.ads.applovin.ApplovinHelper
import com.tqhit.adlib.sdk.ads.applovin.MaxInterstitialHelper
import com.tqhit.adlib.sdk.ads.applovin.MaxRewardedHelper
import com.tqhit.adlib.sdk.ads.applovin.MaxNativeHelper
import com.tqhit.adlib.sdk.firebase.FirebaseRemoteConfigHelper
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityAdLoader @Inject constructor(
    private val remoteConfigHelper: FirebaseRemoteConfigHelper,
    private val admobHelper: AdmobHelper,
    private val applovinHelper: ApplovinHelper
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

        // Remote Config Keys for Ad Unit IDs (fallback when no custom ID in JSON)
        const val RC_INTERSTITIAL_AD_UNIT_ID = "iv_ad_unit_id"
        const val RC_REWARDED_AD_UNIT_ID = "rv_ad_unit_id"
        const val RC_NATIVE_AD_UNIT_ID = "nt_ad_unit_id"
        const val RC_BANNER_AD_UNIT_ID = "bn_ad_unit_id"
        const val RC_C_BANNER_AD_UNIT_ID = "c_bn_ad_unit_id"
        const val RC_AOA_AD_UNIT_ID = "aoa_ad_unit_id"

        // MAX-specific Remote Config keys (fallback when no custom ID in JSON)
        const val RC_MAX_INTERSTITIAL_AD_UNIT_ID = "max_iv_ad_unit_id"
        const val RC_MAX_REWARDED_AD_UNIT_ID = "max_rv_ad_unit_id"
        const val RC_MAX_NATIVE_AD_UNIT_ID = "max_nt_ad_unit_id"
        const val RC_MAX_BANNER_AD_UNIT_ID = "max_bn_ad_unit_id"
        const val RC_MAX_C_BANNER_AD_UNIT_ID = "max_c_bn_ad_unit_id" // optional; fallback to banner
        const val RC_MAX_AOA_AD_UNIT_ID = "max_aoa_ad_unit_id"

        const val RC_ENABLE_ACTIVITY_AD_LOADER = "enable_activity_ad_loader"
    }

    // Data class for ad configuration from JSON
    data class AdConfig(
        val useMax: Boolean,
        val customId: String? = null
    )

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

    /**
     * Parse JSON configuration for a specific ad key from Remote Config
     * Expected JSON format: {"useMax": true/false, "customId": "optional_custom_id"}
     */
    fun getAdConfig(adKey: String): AdConfig? {
        return try {
            val jsonString = remoteConfigHelper.getString(adKey)
            if (jsonString.isBlank()) {
                Log.w(TAG, "No JSON configuration found for ad key: $adKey")
                return null
            }
            
            val json = JSONObject(jsonString)
            val useMax = json.optBoolean("useMax", false)
            val customId = try {
                val id = json.getString("customId")
                if (id.isNotBlank()) id else null
            } catch (e: Exception) {
                null
            }
            
            Log.d(TAG, "Parsed ad config for $adKey: useMax=$useMax, customId=$customId")
            AdConfig(useMax, customId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JSON configuration for ad key: $adKey", e)
            null
        }
    }

    /**
     * Get ad unit ID based on ad key, configuration, and ad type
     */
    private fun getAdUnitId(adKey: String, adConfig: AdConfig): String {
        // If custom ID is provided, use it
        if (!adConfig.customId.isNullOrBlank()) {
            Log.d(TAG, "Using custom ID for $adKey: ${adConfig.customId}")
            return adConfig.customId
        }

        // Otherwise, use fallback Remote Config keys based on ad type and network
        val fallbackKey = when {
            adKey.endsWith(INTERSTITIAL_SUFFIX) -> if (adConfig.useMax) RC_MAX_INTERSTITIAL_AD_UNIT_ID else RC_INTERSTITIAL_AD_UNIT_ID
            adKey.endsWith(REWARDED_SUFFIX) -> if (adConfig.useMax) RC_MAX_REWARDED_AD_UNIT_ID else RC_REWARDED_AD_UNIT_ID
            adKey.endsWith(NATIVE_SUFFIX) -> if (adConfig.useMax) RC_MAX_NATIVE_AD_UNIT_ID else RC_NATIVE_AD_UNIT_ID
            adKey.endsWith(BANNER_SUFFIX) -> if (adConfig.useMax) RC_MAX_BANNER_AD_UNIT_ID else RC_BANNER_AD_UNIT_ID
            adKey.endsWith(COLLAPSIBLE_BANNER_SUFFIX) -> if (adConfig.useMax) RC_MAX_C_BANNER_AD_UNIT_ID else RC_C_BANNER_AD_UNIT_ID
            adKey.endsWith(AOA_SUFFIX) -> if (adConfig.useMax) RC_MAX_AOA_AD_UNIT_ID else RC_AOA_AD_UNIT_ID
            else -> {
                Log.w(TAG, "Unknown ad type suffix for key: $adKey")
                return ""
            }
        }

        val fallbackId = remoteConfigHelper.getString(fallbackKey)
        Log.d(TAG, "Using fallback ID for $adKey: $fallbackId (from key: $fallbackKey)")
        return fallbackId
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

        // Get ad configuration from Remote Config JSON
        val adConfig = getAdConfig(adKey)
        if (adConfig == null) {
            Log.w(TAG, "Failed to get ad configuration for key: $adKey")
            adLoadInProgress.remove(adKey)
            loadedAdsLiveData.remove(adKey)
            return
        }

        val adUnitId = getAdUnitId(adKey, adConfig)
        if (adUnitId.isEmpty()) {
            Log.w(TAG, "Ad Unit ID for $adKey is empty. Cannot load ad.")
            adLoadInProgress.remove(adKey)
            loadedAdsLiveData.remove(adKey)
            return
        }

        when {
            adKey.endsWith(INTERSTITIAL_SUFFIX) -> {
                if (adConfig.useMax) {
                    applovinHelper.loadInterstitial(activity, adUnitId, object : MaxInterstitialAdCallback() {
                        override fun onAdLoaded(interstitialAd: MaxInterstitialAd) {
                            handleAdLoaded(adKey, interstitialAd, "Interstitial", adLiveData)
                        }
                        override fun onAdFailedToLoad(error: com.applovin.mediation.MaxError?) {
                            handleAdFailedToLoad(adKey, error?.message, "Interstitial")
                        }
                    })
                } else {
                    admobHelper.loadInterstitial(activity, adUnitId, 10000, object : InterstitialAdCallback() {
                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            super.onAdLoaded(interstitialAd)
                            handleAdLoaded(adKey, interstitialAd, "Interstitial", adLiveData)
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError?) {
                            super.onAdFailedToLoad(adError)
                            handleAdFailedToLoad(adKey, adError?.message, "Interstitial")
                        }
                    })
                }
            }
            adKey.endsWith(REWARDED_SUFFIX) -> {
                if (adConfig.useMax) {
                    applovinHelper.loadReward(activity, adUnitId, object : MaxRewardAdCallback() {
                        override fun onAdLoaded(rewardedAd: MaxRewardedAd) {
                            handleAdLoaded(adKey, rewardedAd, "Rewarded", adLiveData)
                        }
                        override fun onAdFailedToLoad(error: com.applovin.mediation.MaxError?) {
                            handleAdFailedToLoad(adKey, error?.message, "Rewarded")
                        }
                    })
                } else {
                    admobHelper.loadReward(activity, adUnitId, 10000, object : RewardAdCallback() {
                        override fun onAdLoaded(rewardedAd: RewardedAd) {
                            super.onAdLoaded(rewardedAd)
                            handleAdLoaded(adKey, rewardedAd, "Rewarded", adLiveData)
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError?) {
                            super.onAdFailedToLoad(adError)
                            handleAdFailedToLoad(adKey, adError?.message, "Rewarded")
                        }
                    })
                }
            }
            adKey.endsWith(NATIVE_SUFFIX) -> {
                if (adConfig.useMax) {
                    applovinHelper.loadNative(activity, adUnitId, object : MaxNativeAdCallback() {
                        override fun onAdLoaded(nativeAdView: MaxNativeAdView, loader: MaxNativeAdLoader) {
                            handleAdLoaded(adKey, nativeAdView, "Native", adLiveData)
                        }

                        override fun onAdFailedToLoad(error: com.applovin.mediation.MaxError?) {
                            handleAdFailedToLoad(adKey, error?.message, "Native")
                        }
                    })
                } else {
                    admobHelper.loadNative(activity, adUnitId, 100000, object : NativeAdCallback() {
                        override fun onAdLoaded(nativeAd: NativeAd) {
                            super.onAdLoaded(nativeAd)
                            handleAdLoaded(adKey, nativeAd, "Native", adLiveData)
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError?) {
                            super.onAdFailedToLoad(adError)
                            handleAdFailedToLoad(adKey, adError?.message, "Native")
                        }
                    })
                }
            }
            adKey.endsWith(BANNER_SUFFIX) -> {
                if (adConfig.useMax) {
                    applovinHelper.loadBanner(activity, adUnitId, null, object : MaxBannerAdCallback() {
                        override fun onAdLoaded(adView: MaxAdView) {
                            handleAdLoaded(adKey, adView, "Banner", adLiveData)
                        }
                        override fun onAdFailedToLoad(error: com.applovin.mediation.MaxError?) {
                            handleAdFailedToLoad(adKey, error?.message, "Banner")
                        }
                    })
                } else {
                    admobHelper.loadBanner(activity, adUnitId, 10000, object : BannerAdCallback() {
                        override fun onAdLoaded(adView: AdView) {
                            super.onAdLoaded(adView)
                            handleAdLoaded(adKey, adView, "Banner", adLiveData)
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError?) {
                            super.onAdFailedToLoad(adError)
                            handleAdFailedToLoad(adKey, adError?.message, "Banner")
                        }
                    })
                }
            }
            adKey.endsWith(COLLAPSIBLE_BANNER_SUFFIX) -> {
                if (adConfig.useMax) {
                    // No collapsible in MAX; fallback to standard banner
                    applovinHelper.loadBanner(activity, adUnitId, null, object : MaxBannerAdCallback() {
                        override fun onAdLoaded(adView: MaxAdView) {
                            handleAdLoaded(adKey, adView, "Collapsible Banner", adLiveData)
                        }
                        override fun onAdFailedToLoad(error: com.applovin.mediation.MaxError?) {
                            handleAdFailedToLoad(adKey, error?.message, "Collapsible Banner")
                        }
                    })
                } else {
                    admobHelper.loadCollapsibleBanner(activity, adUnitId, 10000, object : BannerAdCallback() {
                        override fun onAdLoaded(adView: AdView) {
                            super.onAdLoaded(adView)
                            handleAdLoaded(adKey, adView, "Collapsible Banner", adLiveData)
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError?) {
                            super.onAdFailedToLoad(adError)
                            handleAdFailedToLoad(adKey, adError?.message, "Collapsible Banner")
                        }
                    })
                }
            }
            adKey.endsWith(AOA_SUFFIX) -> {
                if (adConfig.useMax) {
                    applovinHelper.setAppOpenAdUnitId(adUnitId)
                    applovinHelper.loadAOA(activity)
                    // For AOA, we don't need to handle the loaded ad in LiveData
                    // as it's managed by the helper itself
                } else {
                    admobHelper.setAppOpenAdUnitId(adUnitId)
                    admobHelper.loadAOA(activity)
                    // For AOA, we don't need to handle the loaded ad in LiveData
                    // as it's managed by the helper itself
                }
            }
            else -> {
                Log.w(TAG, "Unknown ad type suffix for key: $adKey. Cannot determine ad type.")
                adLoadInProgress.remove(adKey)
                loadedAdsLiveData.remove(adKey) // Clean up placeholder LiveData
                return
            }
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
