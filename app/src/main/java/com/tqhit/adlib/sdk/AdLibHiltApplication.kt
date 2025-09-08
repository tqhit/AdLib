package com.tqhit.adlib.sdk

import android.app.Activity
import android.os.Bundle
import androidx.annotation.XmlRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.tqhit.adlib.sdk.adjust.AdjustAnalyticsHelper
import com.tqhit.adlib.sdk.ads.AdmobHelper
import com.tqhit.adlib.sdk.ads.AppOpenHelper
import com.tqhit.adlib.sdk.ads.loader.ActivityAdLoader
import com.tqhit.adlib.sdk.analytics.AnalyticsTracker
import com.tqhit.adlib.sdk.base.AdLibBaseApplication
import com.tqhit.adlib.sdk.firebase.FirebaseRemoteConfigHelper
import javax.inject.Inject

// @HiltAndroidApp
open class AdLibHiltApplication : AdLibBaseApplication() {
    @Inject lateinit var admobHelper: AdmobHelper
    @Inject lateinit var analyticsTracker: AnalyticsTracker
    @Inject lateinit var adjustAnalyticsHelper: AdjustAnalyticsHelper
    @Inject lateinit var remoteConfigHelper: FirebaseRemoteConfigHelper
    @Inject lateinit var activityAdLoader: ActivityAdLoader

    override fun onCreateExt() {
        super.onCreateExt()
    }

    open fun initAdmob(onComplete: () -> Unit, testDeviceIds: List<String>? = null) {
        admobHelper.initAdmob(onComplete, testDeviceIds)
    }

    fun initRemoteConfig(@XmlRes defaultConfig: Int,
                         onFetchComplete: ((Boolean) -> Unit)) {
        remoteConfigHelper.fetchAndActivate(onFetchComplete, defaultConfig)
    }

    fun initTracker(token: String) {
        adjustAnalyticsHelper.initAdjust(token)
    }

    override fun showAOA() {
        super.showAOA()

        if (currentActivity != null) {
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
