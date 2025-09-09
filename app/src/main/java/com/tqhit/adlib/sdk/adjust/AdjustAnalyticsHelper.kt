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
    val coreEventNames = listOf(
        "aj_app_open_displayed",
        "aj_app_open_load",
        "aj_app_open_load_failed",
        "aj_app_open_show",
        "aj_banner_displayed",
        "aj_banner_load",
        "aj_banner_load_failed",
        "aj_inters_displayed",
        "aj_inters_load",
        "aj_inters_load_failed",
        "aj_inters_show",
        "aj_native_displayed",
        "aj_native_load",
        "aj_native_load_failed",
        "aj_reward_displayed",
        "aj_reward_load",
        "aj_reward_load_failed",
        "aj_reward_show"
    )

    var eventMap: Map<String, String> = mapOf()

    fun setEventMap(map: Map<String, String>) {
        eventMap = map
    }

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
        val eventToken = eventMap[eventName] ?: return
        val event = AdjustEvent(eventToken)
        Adjust.trackEvent(event)
    }

    fun trackRevenueEvent(adValue: AdValue, adjustSource: String = "admob_sdk") {
        val adjustAdRevenue = AdjustAdRevenue(adjustSource)
        adjustAdRevenue.setRevenue(adValue.valueMicros / 1000000.0, adValue.currencyCode)
        Adjust.trackAdRevenue(adjustAdRevenue)
    }
}