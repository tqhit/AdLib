package com.tqhit.adlib.sdk.ads

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.tqhit.adlib.sdk.ads.callback.BannerAdCallback
import com.tqhit.adlib.sdk.analytics.AnalyticsTracker
import com.tqhit.adlib.sdk.utils.Constant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BannerHelper @Inject constructor(
    private val admobConsentHelper: AdmobConsentHelper,
    private val analyticsTracker: AnalyticsTracker
) {
    private fun getCollapsibleAdRequest(timeout: Int = 60000): AdRequest {
        val builder = AdRequest.Builder().setHttpTimeoutMillis(timeout)
        val bundle = Bundle()
        bundle.putString("collapsible", "bottom")
        bundle.putString("collapsible_request_id", UUID.randomUUID().toString())
        builder.addNetworkExtrasBundle(AdMobAdapter::class.java, bundle)
        return builder.build()
    }

    private fun getAdRequest(timeout: Int = 60000): AdRequest {
        return AdRequest.Builder().setHttpTimeoutMillis(timeout).build()
    }

    private fun getAdSize(activity: Activity): AdSize {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val i = (displayMetrics.widthPixels / displayMetrics.density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, i)
    }

    fun showCollapsibleBanner(
        activity: Activity,
        bannerAdUnitId: String,
        parent: ViewGroup,
        timeoutMilliSecond: Int?,
        adCallback: BannerAdCallback?
    ) {
        if (!admobConsentHelper.canRequestAds())
        {
            adCallback?.onAdFailedToLoad();
            return
        }
        analyticsTracker.logEvent("aj_banner_load")
        val adView = AdView(activity)
        val adRequest = getCollapsibleAdRequest(timeoutMilliSecond ?: 60000)
        adView.apply {
            adUnitId = if (Constant.DEBUG_MODE) Constant.ADMOB_COLLAPSIBLE_BANNER_AD_UNIT_ID else bannerAdUnitId
            adListener = object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    adCallback?.onAdFailedToLoad(loadAdError)
                    analyticsTracker.logEvent("load_ad_failed", mapOf(
                        "ad_unit_id" to bannerAdUnitId,
                        "ad_error_message" to loadAdError.message
                    ))
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    adCallback?.onAdLoaded()
                    analyticsTracker.logEvent("aj_banner_displayed")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    adCallback?.onAdClicked()
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    adCallback?.onAdImpression()
                }
            }
            onPaidEventListener = OnPaidEventListener { adValue ->
                analyticsTracker.trackRevenueEvent(
                    adValue,
                        adView.responseInfo?.loadedAdapterResponseInfo?.adSourceName ?: "AdMob",
                    "Banner"
                )
            }
        }
        adView.setAdSize(getAdSize(activity))
        adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        adView.loadAd(adRequest)
        parent.addView(adView)
    }

    fun showBanner(
        activity: Activity,
        bannerAdUnitId: String,
        parent: ViewGroup,
        timeoutMilliSecond: Int?,
        adCallback: BannerAdCallback?
    ) {
        if (!admobConsentHelper.canRequestAds())
        {
            adCallback?.onAdFailedToLoad();
            return
        }
        analyticsTracker.logEvent("aj_banner_load")
        val adView = AdView(activity)
        val adRequest = getAdRequest(timeoutMilliSecond ?: 60000)
        adView.apply {
            adUnitId = if (Constant.DEBUG_MODE) Constant.ADMOB_BANNER_AD_UNIT_ID else bannerAdUnitId
            adListener = object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    adCallback?.onAdFailedToLoad(loadAdError)
                    analyticsTracker.logEvent("load_ad_failed", mapOf(
                        "ad_unit_id" to bannerAdUnitId,
                        "ad_error_message" to loadAdError.message
                    ))
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    adCallback?.onAdLoaded()
                    analyticsTracker.logEvent("aj_banner_displayed")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    adCallback?.onAdClicked()
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    adCallback?.onAdImpression()
                }
            }
            onPaidEventListener = OnPaidEventListener { adValue ->
                analyticsTracker.trackRevenueEvent(
                    adValue,
                    adView.responseInfo?.loadedAdapterResponseInfo?.adSourceName ?: "AdMob",
                    "Banner"
                )
            }
        }
        adView.setAdSize(getAdSize(activity))
        adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        adView.loadAd(adRequest)
        parent.addView(adView)
    }
}