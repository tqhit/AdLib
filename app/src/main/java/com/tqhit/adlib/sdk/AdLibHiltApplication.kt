package com.tqhit.adlib.sdk

import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel
import com.tqhit.adlib.sdk.base.AdLibBaseApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
open class AdLibHiltApplication : AdLibBaseApplication() {
    override fun onCreateExt() {
        super.onCreateExt()

        initAdjust()
    }

    private fun initAdjust() {
        val adjustAppToken = "{YourAppToken}"
        val environment = if (isDebugMode()) {
            AdjustConfig.ENVIRONMENT_SANDBOX
        } else {
            AdjustConfig.ENVIRONMENT_PRODUCTION
        }
        val config = AdjustConfig(this, adjustAppToken, environment)
        config.setLogLevel(if (isDebugMode()) LogLevel.VERBOSE else LogLevel.WARN)
        Adjust.initSdk(config)
    }
}