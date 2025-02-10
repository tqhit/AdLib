package com.tqhit.adlib.sdk.ui.main

import android.os.Handler
import android.os.Looper
import com.tqhit.adlib.R
import com.tqhit.adlib.databinding.ActivityMainBinding
import com.tqhit.adlib.sdk.base.ui.AdLibBaseActivity

class MainActivity : AdLibBaseActivity<ActivityMainBinding>() {
    override val layout: Int
        get() = R.layout.activity_main
}