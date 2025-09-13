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
        "aj_app_open_load",
        "aj_app_open_load_success",
        "aj_app_open_load_fail",
        "aj_app_open_show",
        "aj_app_open_show_success",
        "aj_app_open_show_fail",
        "aj_app_open_close",
        "aj_app_open_click",

        "aj_banner_load",
        "aj_banner_load_success",
        "aj_banner_load_fail",
        "aj_banner_show_success",
        "aj_banner_show_fail",
        "aj_banner_close",
        "aj_banner_click",

        "aj_inters_load",
        "aj_inters_load_success",
        "aj_inters_load_fail",
        "aj_inters_show",
        "aj_inters_show_success",
        "aj_inters_show_fail",
        "aj_inters_show_close",
        "aj_inters_show_click",

        "aj_native_load",
        "aj_native_load_success",
        "aj_native_load_fail",
        "aj_native_show_success",
        "aj_native_click",

        "aj_reward_load",
        "aj_reward_load_success",
        "aj_reward_load_fail",
        "aj_reward_show",
        "aj_reward_show_success",
        "aj_reward_show_fail",
        "aj_reward_show_close",
        "aj_reward_show_click"
    )

    var eventMap: Map<String, String> = mapOf()

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

    fun trackRevenueEvent(value: Double, currency: String, adjustSource: String = "admob_sdk") {
        val adjustAdRevenue = AdjustAdRevenue(adjustSource)
        adjustAdRevenue.setRevenue(value, currency)
        Adjust.trackAdRevenue(adjustAdRevenue)
    }
}