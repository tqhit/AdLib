package com.tqhit.adlib.sdk.ads

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.tqhit.adlib.R
import com.tqhit.adlib.sdk.ads.callback.NativeAdCallback
import com.tqhit.adlib.sdk.analytics.AnalyticsTracker
import com.tqhit.adlib.sdk.firebase.FirebaseRemoteConfigHelper
import com.tqhit.adlib.sdk.utils.Constant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NativeHelper @Inject constructor(
    private val admobConsentHelper: AdmobConsentHelper,
    private val analyticsTracker: AnalyticsTracker,
    private val remoteConfigHelper: FirebaseRemoteConfigHelper
) {
    private val enableAd by lazy { remoteConfigHelper.getBoolean("nt_enable") }

    private fun getAdRequest(timeout: Int = 60000): AdRequest {
        return AdRequest.Builder().setHttpTimeoutMillis(timeout).build()
    }

    fun loadNative(
        context: Context,
        nativeAdUnitId: String,
        timeOutMilliSecond: Int?,
        adCallback: NativeAdCallback?
    ) {
        if (!enableAd || !admobConsentHelper.canRequestAds()) {
            adCallback?.onAdFailedToLoad()
            return
        }

        analyticsTracker.logEvent("aj_native_load")

        val adUnitId = if (Constant.DEBUG_MODE) Constant.ADMOB_NATIVE_AD_UNIT_ID else nativeAdUnitId
        val videoOption = VideoOptions.Builder()
            .setStartMuted(true)
            .build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOption)
            .build()
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { nativeAd ->
                nativeAd.apply {
                    setOnPaidEventListener { adValue: AdValue ->
                        analyticsTracker.trackRevenueEvent(
                            adValue,
                            nativeAd.responseInfo?.loadedAdapterResponseInfo?.adSourceName
                                ?: "AdMob",
                            "Native"
                        )
                    }
                }
                adCallback?.onAdLoaded(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adCallback?.onAdFailedToLoad(adError)
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    adCallback?.onAdImpression()
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    adCallback?.onAdClicked()
                }
            })
            .withNativeAdOptions(adOptions)
            .build()
        adLoader.loadAd(getAdRequest(timeOutMilliSecond ?: 60000))
    }

    fun showNative(
        nativeAd: NativeAd,
        nativeAdView: NativeAdView
    ) {
        nativeAdView.mediaView = nativeAdView.findViewById(R.id.ad_media)
        nativeAdView.headlineView = nativeAdView.findViewById(R.id.ad_headline)
        nativeAdView.bodyView = nativeAdView.findViewById(R.id.ad_body)
        nativeAdView.callToActionView = nativeAdView.findViewById(R.id.ad_call_to_action)
        nativeAdView.iconView = nativeAdView.findViewById(R.id.ad_app_icon)
        nativeAdView.advertiserView = nativeAdView.findViewById(R.id.ad_advertiser)

        nativeAdView.headlineView?.let {
            (it as TextView).text = nativeAd.headline
        }

        nativeAdView.bodyView?.let {
            if (nativeAd.body == null) {
                it.visibility = View.INVISIBLE
            } else {
                it.visibility = View.VISIBLE
                (it as TextView).text = nativeAd.body
            }
        }

        nativeAdView.callToActionView?.let {
            if (nativeAd.callToAction == null) {
                it.visibility = View.INVISIBLE
            } else {
                it.visibility = View.VISIBLE
                if (it is TextView) {
                    it.text = nativeAd.callToAction
                }
                if (it is AppCompatButton) {
                    it.text = nativeAd.callToAction
                }
            }
        }

        nativeAdView.iconView?.let {
            if (nativeAd.icon == null) {
                it.visibility = View.GONE
            } else {
                it.visibility = View.VISIBLE
                (it as ImageView).setImageDrawable(nativeAd.icon?.drawable)
            }
        }

        nativeAdView.advertiserView?.let {
            if (nativeAd.advertiser == null) {
                it.visibility = View.GONE
            } else {
                it.visibility = View.VISIBLE
                (it as TextView).text = nativeAd.advertiser
            }
        }

        nativeAdView.priceView?.let {
            if (nativeAd.price == null) {
                it.visibility = View.GONE
            } else {
                it.visibility = View.VISIBLE
                (it as TextView).text = nativeAd.price
            }
        }

        nativeAdView.storeView?.let {
            if (nativeAd.store == null) {
                it.visibility = View.GONE
            } else {
                it.visibility = View.VISIBLE
                (it as TextView).text = nativeAd.store
            }
        }

        nativeAdView.starRatingView?.let {
            if (nativeAd.starRating == null) {
                it.visibility = View.GONE
            } else {
                it.visibility = View.VISIBLE
                (it as RatingBar).rating = nativeAd.starRating!!.toFloat()
            }
        }

        nativeAdView.setNativeAd(nativeAd)
        analyticsTracker.logEvent("aj_native_displayed")
    }
}