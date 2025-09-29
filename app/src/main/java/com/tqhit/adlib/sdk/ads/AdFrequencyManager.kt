package com.tqhit.adlib.sdk.ads

import com.tqhit.adlib.sdk.firebase.FirebaseRemoteConfigHelper
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Global frequency manager for cross-network ad frequency and delay controls
 * This ensures that frequency and delay rules work across both AdMob and AppLovin MAX
 * and applies globally to all ad slots/placements
 */
@Singleton
class AdFrequencyManager @Inject constructor(
    private val remoteConfigHelper: FirebaseRemoteConfigHelper
) {
    // Global tracking for all ad types across all networks (not per adKey)
    private var lastInterstitialShowTime: Long = 0L
    private var lastRewardedShowTime: Long = 0L
    
    companion object {
        private const val RC_IV_SHOW_FREQUENCY = "iv_show_frequency"
        private const val RC_IV_DELAY_SHOW_AFTER_RV = "iv_delay_show_after_rv"
    }
    
    /**
     * Check if interstitial ad can be shown based on frequency and delay rules
     * This works across both AdMob and AppLovin MAX networks globally
     */
    fun canShowInterstitial(): Boolean {
        val currentTime = System.currentTimeMillis()
        val frequencySeconds = remoteConfigHelper.getLong(RC_IV_SHOW_FREQUENCY)
        val delayAfterRewardedSeconds = remoteConfigHelper.getLong(RC_IV_DELAY_SHOW_AFTER_RV)
        
        // Check frequency rule (time between interstitial ads)
        val timeSinceLastIv = (currentTime - lastInterstitialShowTime) / 1000
        if (frequencySeconds > 0 && timeSinceLastIv < frequencySeconds) {
            return false
        }
        
        // Check delay after rewarded rule (time since any rewarded ad was shown)
        val timeSinceLastRv = (currentTime - lastRewardedShowTime) / 1000
        if (delayAfterRewardedSeconds > 0 && timeSinceLastRv < delayAfterRewardedSeconds) {
            return false
        }
        
        return true
    }
    
    /**
     * Record that an interstitial ad was shown
     * This works across both AdMob and AppLovin MAX networks
     */
    fun recordInterstitialShown() {
        lastInterstitialShowTime = System.currentTimeMillis()
    }
    
    /**
     * Record that a rewarded ad was shown
     * This works across both AdMob and AppLovin MAX networks
     */
    fun recordRewardedShown() {
        lastRewardedShowTime = System.currentTimeMillis()
    }
}
