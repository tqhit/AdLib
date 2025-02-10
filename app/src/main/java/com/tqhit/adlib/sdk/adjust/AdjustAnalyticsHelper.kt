package com.tqhit.adlib.sdk.adjust

import android.content.Context
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.LogLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdjustAnalyticsHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun initAdjust(isDebugMode: Boolean, adjustAppToken: String) {
        val environment = if (isDebugMode) {
            AdjustConfig.ENVIRONMENT_SANDBOX
        } else {
            AdjustConfig.ENVIRONMENT_PRODUCTION
        }

        val config = AdjustConfig(context, adjustAppToken, environment)  // Use application context
        config.setLogLevel(if (isDebugMode) LogLevel.VERBOSE else LogLevel.WARN)

        Adjust.initSdk(config)
    }

    fun trackEvent(eventToken: String, revenue: Double? = null, currency: String? = "USD") {
        val event = AdjustEvent(eventToken)
        revenue?.let { event.setRevenue(it, currency) }
        Adjust.trackEvent(event)
    }
}