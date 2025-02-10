package com.tqhit.adlib.sdk

import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel
import com.tqhit.adlib.sdk.adjust.AdjustAnalyticsHelper
import com.tqhit.adlib.sdk.base.AdLibBaseApplication
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
open class AdLibHiltApplication : AdLibBaseApplication() {
    @Inject
    lateinit var adjustAnalyticsHelper: AdjustAnalyticsHelper

    override fun onCreateExt() {
        super.onCreateExt()

        adjustAnalyticsHelper.initAdjust(isDebugMode(), "{token")
    }
}