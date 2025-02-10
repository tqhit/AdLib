package com.tqhit.adlib.sdk.analytics

import com.tqhit.adlib.sdk.adjust.AdjustAnalyticsHelper
import com.tqhit.adlib.sdk.firebase.FirebaseAnalyticsHelper
import javax.inject.Inject

class AnalyticsTracker @Inject constructor(
    private val firebaseAnalyticsHelper: FirebaseAnalyticsHelper,
    private val adjustAnalyticsHelper: AdjustAnalyticsHelper
) {

    fun logEvent(eventName: String, params: Map<String, Any>? = null) {
        firebaseAnalyticsHelper.logEvent(eventName, params)
    }

    fun trackEvent(eventToken: String, revenue: Double? = null, currency: String? = "USD") {
        adjustAnalyticsHelper.trackEvent(eventToken, revenue, currency)
    }
}