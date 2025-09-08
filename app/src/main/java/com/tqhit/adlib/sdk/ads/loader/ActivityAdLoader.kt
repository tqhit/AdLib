package com.tqhit.adlib.sdk.ads.loader

import android.app.Activity
import android.util.Log
import com.tqhit.adlib.sdk.ads.AdmobHelper
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
    private val loadedAds = mutableMapOf<String, Any>()
    private val loadingAds = mutableSetOf<String>()

    companion object {
        private const val TAG = "ActivityAdLoader"
        const val INTERSTITIAL_SUFFIX = "_IV"
        const val REWARDED_SUFFIX = "_RV"
        const val NATIVE_SUFFIX = "_NT"
        const val AOA_SUFFIX = "_AOA"
    }

    fun onActivityCreated(activity: Activity, fragmentName: String? = null) {
        if (remoteConfigHelper.getBoolean("enable_activity_ad_loader").not()) {
            Log.d(TAG, "ActivityAdLoader is disabled via remote config.")
            return
        }

        val activityName = activity.javaClass.simpleName + (fragmentName?.let { "_$it" } ?: "")

        Log.d(TAG, "onActivityCreated for: $activityName")
        loadAdForActivity(activity, activityName, INTERSTITIAL_SUFFIX)
        loadAdForActivity(activity, activityName, REWARDED_SUFFIX)
        loadAdForActivity(activity, activityName, NATIVE_SUFFIX)
        loadAdForActivity(activity, activityName, AOA_SUFFIX)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getAd(key: String): T? {
        val ad = loadedAds[key] as? T
        if (ad != null) {
            Log.d(TAG, "Retrieved ad for key: $key")
        } else {
            Log.d(TAG, "No ad found in cache for key: $key")
        }
        return ad
    }

    fun removeKeyConcise(key: String) {
        val removedAd = loadedAds.remove(key)
        if (removedAd != null) {
            Log.d(TAG, "Removed ad for key: $key, Ad was: $removedAd")
        } else {
            Log.d(TAG, "Attempted to remove key: $key, but it was not found in the map.")
        }
    }

    private fun loadAdForActivity(activity: Activity, activityName: String, adTypeSuffix: String) {
        val remoteConfigKey = "${activityName}${adTypeSuffix}"
        val shouldLoadAd = remoteConfigHelper.getBoolean(remoteConfigKey)

        Log.d(TAG, "Checking $remoteConfigKey: shouldLoadAd = $shouldLoadAd")

        if (shouldLoadAd) {
            if (loadingAds.contains(remoteConfigKey) || loadedAds.containsKey(remoteConfigKey)) {
                Log.d(TAG, "Ad for $remoteConfigKey already loaded or load in progress. Skipping.")
                return
            }

            Log.d(TAG, "Remote config for $remoteConfigKey is TRUE. Attempting to load ad.")
            loadingAds.add(remoteConfigKey)

            val adUnitIdKey = when (adTypeSuffix) {
                INTERSTITIAL_SUFFIX -> "iv_ad_unit_id"
                REWARDED_SUFFIX -> "rv_ad_unit_id"
                NATIVE_SUFFIX -> "nt_ad_unit_id"
                AOA_SUFFIX -> "aoa_ad_unit_id"
                else -> ""
            }
            val adUnitId = remoteConfigHelper.getString(adUnitIdKey)

            if (adUnitId.isEmpty()) {
                Log.w(TAG, "Ad Unit ID for $adUnitIdKey is null or empty in Remote Config. Cannot load $remoteConfigKey.")
                loadingAds.remove(remoteConfigKey)
                return
            }
            Log.d(TAG, "Using Ad Unit ID: $adUnitId for $remoteConfigKey")

            when (adTypeSuffix) {
                INTERSTITIAL_SUFFIX -> {
                    admobHelper.loadInterstitial(activity, adUnitId, 10000, object : InterstitialAdCallback() {
                        override fun onAdLoaded(interstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd) {
                            Log.d(TAG, "Interstitial ad loaded for $remoteConfigKey")
                            loadedAds[remoteConfigKey] = interstitialAd
                            loadingAds.remove(remoteConfigKey)
                        }

                        override fun onAdFailedToLoad(adError: com.google.android.gms.ads.LoadAdError?) {
                            Log.w(TAG, "Failed to load interstitial ad for $remoteConfigKey: ${adError?.message}")
                            loadingAds.remove(remoteConfigKey)
                        }
                    })
                }
                REWARDED_SUFFIX -> {
                    admobHelper.loadReward(activity, adUnitId, 10000, object : RewardAdCallback() {
                        override fun onAdLoaded(rewardedAd: com.google.android.gms.ads.rewarded.RewardedAd) {
                            Log.d(TAG, "Rewarded ad loaded for $remoteConfigKey")
                            loadedAds[remoteConfigKey] = rewardedAd
                            loadingAds.remove(remoteConfigKey)
                        }

                        override fun onAdFailedToLoad(adError: com.google.android.gms.ads.LoadAdError?) {
                            Log.w(TAG, "Failed to load rewarded ad for $remoteConfigKey: ${adError?.message}")
                            loadingAds.remove(remoteConfigKey)
                        }
                    })
                }
                NATIVE_SUFFIX -> {
                    admobHelper.loadNative(activity, adUnitId, 10000, object : NativeAdCallback() {
                        override fun onAdLoaded(nativeAd: com.google.android.gms.ads.nativead.NativeAd) {
                            Log.d(TAG, "Native ad loaded for $remoteConfigKey")
                            loadedAds[remoteConfigKey] = nativeAd
                            loadingAds.remove(remoteConfigKey)
                        }

                        override fun onAdFailedToLoad(adError: com.google.android.gms.ads.LoadAdError?) {
                            Log.w(TAG, "Failed to load native ad for $remoteConfigKey: ${adError?.message}")
                            loadingAds.remove(remoteConfigKey)
                        }
                    })
                }
                AOA_SUFFIX -> {
                    admobHelper.loadAOA(activity)
                }
            }
        } else {
            Log.d(TAG, "Remote config for $remoteConfigKey is FALSE or not found. Not loading ad.")
        }
    }
}