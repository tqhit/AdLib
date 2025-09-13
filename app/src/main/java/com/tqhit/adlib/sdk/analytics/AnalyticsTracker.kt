package com.tqhit.adlib.sdk.analytics

import com.applovin.mediation.MaxAd
import com.google.android.gms.ads.AdValue
import com.google.firebase.analytics.FirebaseAnalytics
import com.tqhit.adlib.sdk.adjust.AdjustAnalyticsHelper
import com.tqhit.adlib.sdk.firebase.FirebaseAnalyticsHelper
import com.tqhit.adlib.sdk.utils.Constant
import javax.inject.Inject
import kotlin.div

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

    fun trackAdMobRevenueEvent(
        adValue: AdValue,
        adUnitId: String,
        adSource: String,
        adFormat: String,
    ) {
        if (Constant.DEBUG_MODE) return
        firebaseAnalyticsHelper.logEvent(
                "ad_impression_custom",
                mapOf(
                        FirebaseAnalytics.Param.AD_PLATFORM to "admob",
                        FirebaseAnalytics.Param.AD_UNIT_NAME to adUnitId,
                        FirebaseAnalytics.Param.AD_SOURCE to adSource,
                        FirebaseAnalytics.Param.AD_FORMAT to adFormat,
                        FirebaseAnalytics.Param.VALUE to adValue.valueMicros.toDouble() / 1000000.0,
                        FirebaseAnalytics.Param.CURRENCY to "USD"
                )
        )
        // https://dev.adjust.com/en/sdk/android/features/ad-revenue/
        adjustAnalyticsHelper.trackRevenueEvent(
            adValue.valueMicros / 1000000.0,
            adValue.currencyCode,
            "admob_sdk")
    }

    fun trackMaxRevenueEvent(
        impressionData: MaxAd?
    ) {
        if (Constant.DEBUG_MODE) return
        impressionData?.let {
            firebaseAnalyticsHelper.logEvent(
                "ad_impression_custom",
                mapOf(
                    FirebaseAnalytics.Param.AD_PLATFORM to "appLovin",
                    FirebaseAnalytics.Param.AD_UNIT_NAME to impressionData.adUnitId,
                    FirebaseAnalytics.Param.AD_FORMAT to impressionData.format.label,
                    FirebaseAnalytics.Param.AD_SOURCE to impressionData.networkName,
                    FirebaseAnalytics.Param.VALUE to impressionData.revenue,
                    FirebaseAnalytics.Param.CURRENCY to "USD"
                )
            )

            // https://dev.adjust.com/en/sdk/android/features/ad-revenue/
            adjustAnalyticsHelper.trackRevenueEvent(
                impressionData.revenue,
                "USD",
                "applovin_max_sdk"
            )

            // https://firebase.google.com/docs/analytics/measure-ad-revenue#implementation-other-platforms
            firebaseAnalyticsHelper.logEvent(
                FirebaseAnalytics.Event.AD_IMPRESSION,
                mapOf(
                    FirebaseAnalytics.Param.AD_PLATFORM to "appLovin",
                    FirebaseAnalytics.Param.AD_UNIT_NAME to impressionData.adUnitId,
                    FirebaseAnalytics.Param.AD_FORMAT to impressionData.format.label,
                    FirebaseAnalytics.Param.AD_SOURCE to impressionData.networkName,
                    FirebaseAnalytics.Param.VALUE to impressionData.revenue,
                    FirebaseAnalytics.Param.CURRENCY to "USD"
                )
            )
        }
    }
}
