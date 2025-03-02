package com.tqhit.adlib.sdk.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.google.android.gms.ads.AdActivity
import com.tqhit.adlib.sdk.ui.crash.AdLibCustomCrashActivity
import com.tqhit.adlib.sdk.utils.Constant

abstract class AdLibBaseApplication : Application(), Application.ActivityLifecycleCallbacks {
    protected var currentActivity: Activity? = null

    open fun onCreateExt() {}

    open fun isDebugMode(): Boolean {
        return Constant.DEBUG_MODE
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

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is AdActivity) return
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected open fun onMoveToForeground() {
    }
}