package com.tqhit.adlib.sdk.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.tqhit.adlib.R

class LoadingAdsDialog(context: Context) : Dialog(context, R.style.AdsAppTheme) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_prepair_loading_ads)
    }

    fun hideLoadingAdsText() {
        findViewById<View>(R.id.loading_dialog_tv).visibility = View.INVISIBLE
    }
}
