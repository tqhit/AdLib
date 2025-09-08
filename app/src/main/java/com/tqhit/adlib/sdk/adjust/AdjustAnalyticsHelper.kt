package com.tqhit.adlib.sdk.adjust

import android.content.Context
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.LogLevel
import com.google.android.gms.ads.AdValue
import com.tqhit.adlib.sdk.utils.Constant
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdjustAnalyticsHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun initAdjust(token: String) {
        val environment = if (Constant.DEBUG_MODE) {
            AdjustConfig.ENVIRONMENT_SANDBOX
        } else {
            AdjustConfig.ENVIRONMENT_PRODUCTION
        }

        val config = AdjustConfig(context, token, environment)
        config.setLogLevel(if (Constant.DEBUG_MODE) LogLevel.VERBOSE else LogLevel.WARN)

        Adjust.initSdk(config)
    }

    fun trackEvent(eventName: String) {
        val event = AdjustEvent(eventName)
        Adjust.trackEvent(event)
    }

    fun trackRevenueEvent(adValue: AdValue) {
        val adjustAdRevenue = AdjustAdRevenue("admob_sdk")
        adjustAdRevenue.setRevenue(adValue.valueMicros / 1000000.0, adValue.currencyCode)
        Adjust.trackAdRevenue(adjustAdRevenue)
    }
}