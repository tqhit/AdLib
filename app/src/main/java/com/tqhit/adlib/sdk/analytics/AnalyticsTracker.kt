package com.tqhit.adlib.sdk.analytics

import com.google.android.gms.ads.AdValue
import com.google.firebase.analytics.FirebaseAnalytics
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

    fun trackRevenueEvent(adValue: AdValue, adSource: String, adFormat: String) {
        adjustAnalyticsHelper.trackRevenueEvent(adValue)

        firebaseAnalyticsHelper.logEvent("ad_impression_custom", mapOf(
            FirebaseAnalytics.Param.AD_PLATFORM to "admob mediation",
            FirebaseAnalytics.Param.AD_SOURCE to adSource,
            FirebaseAnalytics.Param.AD_FORMAT to adFormat,
            FirebaseAnalytics.Param.VALUE to adValue.valueMicros.toDouble() / 1000000.0,
            FirebaseAnalytics.Param.CURRENCY to "USD"
        ))
    }
}