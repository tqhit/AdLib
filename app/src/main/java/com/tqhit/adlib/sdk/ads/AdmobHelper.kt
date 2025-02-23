package com.tqhit.adlib.sdk.ads

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdmobHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bannerHelper: BannerHelper,
    private val interstitialHelper: InterstitialHelper,
    private val rewardHelper: RewardHelper,
    private val nativeHelper: NativeHelper
) {
    private val TAG : String = AdmobHelper::class.java.simpleName

    fun initAdmob() {
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    //.setTestDeviceIds(listOf("FDB0ED7D82BA59AC8B05C2927D8CAD83"))
                    .build()
            )
            MobileAds.initialize(context) { _ ->
                Log.d(TAG, "Admob initialized")
            }
        }
    }
}