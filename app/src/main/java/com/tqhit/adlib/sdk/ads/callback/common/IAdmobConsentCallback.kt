package com.tqhit.adlib.sdk.ads.callback.common

import com.google.android.ump.FormError

interface IAdmobConsentCallback {
    fun consentGatheringComplete(error: FormError?)
}