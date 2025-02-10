package com.tqhit.adlib.sdk.analytics

import com.tqhit.adlib.sdk.firebase.FirebaseAnalyticsHelper
import javax.inject.Inject

class AnalyticsTracker @Inject constructor(
    private val firebaseAnalyticsHelper: FirebaseAnalyticsHelper
) {

    fun logEvent(eventName: String, params: Map<String, Any>? = null) {
        firebaseAnalyticsHelper.logEvent(eventName, params)
    }
}