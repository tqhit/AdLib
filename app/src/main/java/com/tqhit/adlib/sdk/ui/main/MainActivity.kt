package com.tqhit.adlib.sdk.ui.main

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.ump.FormError
import com.tqhit.adlib.R
import com.tqhit.adlib.databinding.ActivityMainBinding
import com.tqhit.adlib.sdk.ads.AdmobConsentHelper
import com.tqhit.adlib.sdk.ads.callback.IAdmobConsentCallback
import com.tqhit.adlib.sdk.base.ui.AdLibBaseActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AdLibBaseActivity<ActivityMainBinding>() {
    override val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    @Inject lateinit var admobConsentHelper: AdmobConsentHelper

    override fun setupData() {
        super.setupData()

        Looper.getMainLooper().let {
            Handler(it).postDelayed({
                admobConsentHelper.gatherConsent(this, object : IAdmobConsentCallback {
                    override fun consentGatheringComplete(error: FormError?) {
                        Log.d("AdmobConsentHelper", "consentGatheringComplete: $error")
                    }
                })
            }, 3000)
        }
    }
}