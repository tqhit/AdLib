package com.tqhit.adlib.sdk.adjust

import android.content.Context
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdjustAnalyticsHelper @Inject constructor(context: Context) {

    fun trackEvent(eventToken: String, revenue: Double? = null, currency: String? = "USD") {
        val event = AdjustEvent(eventToken)
        revenue?.let { event.setRevenue(it, currency) }
        Adjust.trackEvent(event)
    }
}