package com.tqhit.adlib.sdk

import com.tqhit.adlib.sdk.adjust.AdjustAnalyticsHelper
import com.tqhit.adlib.sdk.ads.AdmobHelper
import com.tqhit.adlib.sdk.base.AdLibBaseApplication
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

//@HiltAndroidApp
open class AdLibHiltApplication : AdLibBaseApplication() {
//    @Inject lateinit var adjustAnalyticsHelper: AdjustAnalyticsHelper
//    @Inject lateinit var admobHelper: AdmobHelper

    override fun onCreateExt() {
        super.onCreateExt()

//        admobHelper.initAdmob()
//        adjustAnalyticsHelper.initAdjust()
    }
}