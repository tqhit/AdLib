package com.tqhit.adlib.sdk.base

import android.app.Activity
import android.app.Application
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.tqhit.adlib.sdk.ui.crash.AdLibCustomCrashActivity

abstract class AdLibBaseApplication : Application() {
    open fun onCreateExt() {}

    open fun isDebugMode(): Boolean {
        return true
    }

    open fun setupCAOC() {
        CaocConfig.Builder.create()
            .enabled(true) // default: true
            .showErrorDetails(true) // default: true
            .showRestartButton(true) // default: true
            .logErrorOnRestart(true) // default: true
            .trackActivities(true) // default: false
            .minTimeBetweenCrashesMs(3000) //default: 3000
            .errorActivity(customErrorActivity()) //default: null (default error activity)
            .apply()
    }

    open fun customErrorActivity(): Class<out Activity> {
        return AdLibCustomCrashActivity::class.java
    }

    override fun onCreate() {
        super.onCreate()
        onCreateExt()
        if (isDebugMode()) {
            setupCAOC()
        }
    }
}