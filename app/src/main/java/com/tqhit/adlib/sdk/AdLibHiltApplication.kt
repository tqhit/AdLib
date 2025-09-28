package com.tqhit.adlib.sdk

import android.app.Activity
import android.os.Bundle
import androidx.annotation.XmlRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.tqhit.adlib.sdk.adjust.AdjustAnalyticsHelper
import com.tqhit.adlib.sdk.ads.admob.AdmobHelper
import com.tqhit.adlib.sdk.ads.admob.AppOpenHelper
import com.tqhit.adlib.sdk.ads.applovin.ApplovinHelper
import com.tqhit.adlib.sdk.ads.applovin.MaxAppOpenHelper
import com.tqhit.adlib.sdk.ads.loader.ActivityAdLoader
import com.tqhit.adlib.sdk.analytics.AnalyticsTracker
import com.tqhit.adlib.sdk.base.AdLibBaseApplication
import com.tqhit.adlib.sdk.firebase.FirebaseRemoteConfigHelper
import javax.inject.Inject

// @HiltAndroidApp
open class AdLibHiltApplication : AdLibBaseApplication() {
    protected val APP_AOA_CONFIG_KEY = "APP_AOA"

    @Inject lateinit var admobHelper: AdmobHelper
    @Inject lateinit var applovinHelper: ApplovinHelper
    @Inject lateinit var analyticsTracker: AnalyticsTracker
    @Inject lateinit var adjustAnalyticsHelper: AdjustAnalyticsHelper
    @Inject lateinit var remoteConfigHelper: FirebaseRemoteConfigHelper
    @Inject lateinit var activityAdLoader: ActivityAdLoader

    override fun onCreateExt() {
        super.onCreateExt()
    }

    fun initRemoteConfig(@XmlRes defaultConfig: Int,
                         onFetchComplete: ((Boolean) -> Unit)) {
        remoteConfigHelper.fetchAndActivate(onFetchComplete, defaultConfig)
    }

    fun initTracker(token: String) {
        adjustAnalyticsHelper.initAdjust(token)
    }

    fun initAOA() {
        if (currentActivity == null) return

        val adConfig = activityAdLoader.getAdConfig(APP_AOA_CONFIG_KEY)
        val useMax = adConfig?.useMax ?: false
        val customId = adConfig?.customId
        
        val adUnitId = if (!customId.isNullOrBlank()) {
            customId
        } else {
            if (useMax) {
                remoteConfigHelper.getString(ActivityAdLoader.RC_MAX_AOA_AD_UNIT_ID)
            } else {
                remoteConfigHelper.getString(ActivityAdLoader.RC_AOA_AD_UNIT_ID)
            }
        }

        if (useMax) {
            applovinHelper.setAppOpenAdUnitId(adUnitId)
            applovinHelper.loadAOA(currentActivity!!)
        } else {
            admobHelper.setAppOpenAdUnitId(adUnitId)
            admobHelper.loadAOA(currentActivity!!)
        }
    }

    override fun showAOA() {
        super.showAOA()

        if (currentActivity == null) return

        val adConfig = activityAdLoader.getAdConfig(APP_AOA_CONFIG_KEY)
        val useMax = adConfig?.useMax ?: false

        if (useMax) {
            applovinHelper.showAOA(
                currentActivity!!,
                object : MaxAppOpenHelper.OnShowAdCompleteListener {
                    override fun onShowAdComplete() {}
                }
            )
        } else {
            admobHelper.showAOA(
                currentActivity!!,
                object : AppOpenHelper.OnShowAdCompleteListener {
                    override fun onShowAdComplete() {}
                }
            )
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)

        analyticsTracker.logEvent("view_${activity.javaClass.simpleName.lowercase()}")

        if (activity is FragmentActivity) {
            val fm: FragmentManager = activity.supportFragmentManager
            fm.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentCreated(fm: FragmentManager, f: androidx.fragment.app.Fragment, savedInstanceState: Bundle?) {
                    super.onFragmentCreated(fm, f, savedInstanceState)
                    analyticsTracker.logEvent("view_${f.javaClass.simpleName.lowercase()}")
                }

                override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                    super.onFragmentResumed(fm, f)
                    activityAdLoader.onActivityResumed(activity, f.javaClass.simpleName)
                }
            }, true)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)

        activityAdLoader.onActivityResumed(activity)
    }
}
