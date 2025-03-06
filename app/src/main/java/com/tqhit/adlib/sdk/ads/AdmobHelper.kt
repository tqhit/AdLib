package com.tqhit.adlib.sdk.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.tqhit.adlib.sdk.ads.callback.BannerAdCallback
import com.tqhit.adlib.sdk.ads.callback.InterstitialAdCallback
import com.tqhit.adlib.sdk.ads.callback.NativeAdCallback
import com.tqhit.adlib.sdk.ads.callback.RewardAdCallback
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
    private val nativeHelper: NativeHelper,
    private val appOpenHelper: AppOpenHelper
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

    fun isNetwork(context: Context?): Boolean {
        if (context == null) return false
        val systemService = context.getSystemService(Context.CONNECTIVITY_SERVICE)
        val connectivityManager = systemService as ConnectivityManager
        if (connectivityManager.activeNetworkInfo != null) {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo?.isConnected == true) {
                return true
            }
        }
        return false
    }

    fun showCollapsibleBanner(
        activity: Activity,
        bannerAdUnitId: String,
        parent: ViewGroup,
        timeoutMilliSecond: Int?,
        adCallback: BannerAdCallback?
    ) {
        bannerHelper.showCollapsibleBanner(
            activity,
            bannerAdUnitId,
            parent,
            timeoutMilliSecond,
            adCallback
        )
    }

    fun showBanner(
        activity: Activity,
        bannerAdUnitId: String,
        parent: ViewGroup,
        timeoutMilliSecond: Int?,
        adCallback: BannerAdCallback?
    ) {
        bannerHelper.showBanner(
            activity,
            bannerAdUnitId,
            parent,
            timeoutMilliSecond,
            adCallback
        )
    }

    fun loadInterstitial(
        activity: Activity,
        interstitialAdUnitId: String,
        timeoutMilliSecond: Int?,
        adCallback: InterstitialAdCallback?
    ) {
        interstitialHelper.loadInterstitial(
            activity,
            interstitialAdUnitId,
            timeoutMilliSecond,
            adCallback
        )
    }

    fun showInterstitial(
        activity: Activity,
        interstitialAd: InterstitialAd,
        adCallback: InterstitialAdCallback?
    ) {
        interstitialHelper.showInterstitial(
            activity,
            interstitialAd,
            adCallback
        )
    }

    fun showInterstitial(
        activity: Activity,
        interstitialAdUnitId: String,
        interstitialAd: InterstitialAd?,
        timeoutMilliSecond: Int?,
        adCallback: InterstitialAdCallback?
    ) {
        interstitialHelper.showInterstitial(
            activity,
            interstitialAdUnitId,
            interstitialAd,
            timeoutMilliSecond,
            adCallback
        )
    }

    fun loadReward(
        activity: Activity,
        rewardAdUnitId: String,
        timeOutMilliSecond: Int?,
        adCallback: RewardAdCallback?
    ) {
        rewardHelper.loadReward(
            activity,
            rewardAdUnitId,
            timeOutMilliSecond,
            adCallback
        )
    }

    fun showReward(
        activity: Activity,
        rewardedAd: RewardedAd,
        adCallback: RewardAdCallback?
    ) {
        rewardHelper.showReward(
            activity,
            rewardedAd,
            adCallback
        )
    }

    fun showReward(
        activity: Activity,
        rewardAdUnitId: String,
        rewardedAd: RewardedAd?,
        timeOutMilliSecond: Int?,
        adCallback: RewardAdCallback?
    ) {
        rewardHelper.showReward(
            activity,
            rewardAdUnitId,
            rewardedAd,
            timeOutMilliSecond,
            adCallback
        )
    }

    fun loadNative(
        context: Context,
        nativeAdUnitId: String,
        timeOutMilliSecond: Int?,
        adCallback: NativeAdCallback?
    ) {
        nativeHelper.loadNative(
            context,
            nativeAdUnitId,
            timeOutMilliSecond,
            adCallback
        )
    }

    fun preloadNative(
        context: Context,
        nativeAdUnitId: String,
        timeOutMilliSecond: Int?,
        adCallback: NativeAdCallback?,
        nativeLiveData: MutableLiveData<Any>
    ) {
        nativeHelper.preloadNative(
            context,
            nativeAdUnitId,
            timeOutMilliSecond,
            adCallback,
            nativeLiveData
        )
    }

    fun showNative(
        nativeAd: NativeAd,
        nativeAdView: NativeAdView
    ) {
        nativeHelper.showNative(
            nativeAd,
            nativeAdView
        )
    }

    fun setAppOpenAdUnitId(adUnitId: String) {
        appOpenHelper.setAdUnitId(adUnitId)
    }

    fun showAOA(
        activity: Activity,
        adCallback: AppOpenHelper.OnShowAdCompleteListener?
    ) {
        appOpenHelper.showAdIfAvailable(activity, object : AppOpenHelper.OnShowAdCompleteListener {
            override fun onShowAdComplete() {
                adCallback?.onShowAdComplete()
            }
        })
    }
}