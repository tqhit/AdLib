package com.tqhit.adlib.sdk.analytics

import com.google.android.gms.ads.AdValue
import com.google.firebase.analytics.FirebaseAnalytics
import com.tqhit.adlib.sdk.adjust.AdjustAnalyticsHelper
import com.tqhit.adlib.sdk.firebase.FirebaseAnalyticsHelper
import com.tqhit.adlib.sdk.utils.Constant
import javax.inject.Inject

class AnalyticsTracker
@Inject
constructor(
        private val firebaseAnalyticsHelper: FirebaseAnalyticsHelper,
        private val adjustAnalyticsHelper: AdjustAnalyticsHelper
) {

    fun logEvent(eventName: String, params: Map<String, Any>? = null) {
        if (Constant.DEBUG_MODE) return
        firebaseAnalyticsHelper.logEvent(eventName, params)
        adjustAnalyticsHelper.trackEvent(eventName)
    }

    fun trackRevenueEvent(adValue: AdValue, adSource: String, adFormat: String) {
        if (Constant.DEBUG_MODE) return
        firebaseAnalyticsHelper.logEvent(
                "ad_impression_custom",
                mapOf(
                        FirebaseAnalytics.Param.AD_PLATFORM to "admob",
                        FirebaseAnalytics.Param.AD_SOURCE to adSource,
                        FirebaseAnalytics.Param.AD_FORMAT to adFormat,
                        FirebaseAnalytics.Param.VALUE to adValue.valueMicros.toDouble() / 1000000.0,
                        FirebaseAnalytics.Param.CURRENCY to "USD"
                )
        )
        adjustAnalyticsHelper.trackRevenueEvent(adValue)
    }
}
