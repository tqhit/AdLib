package com.tqhit.adlib.sdk.base.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.graphics.drawable.toDrawable
import androidx.viewbinding.ViewBinding
import com.tqhit.adlib.R

abstract class AdLibBaseDialog<T: ViewBinding>(context: Context) : Dialog(context) {
    abstract val binding: T

    protected open fun setupData() {}
    protected open fun setupListener() {}
    protected open fun setupUI() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        initWindow()
        setupData()
        setupUI()
        setupListener()
    }

    fun setDialogBottom() {
        window?.setGravity(Gravity.BOTTOM)
    }

    open fun initWindow() {
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.attributes?.windowAnimations = R.style.DialogAnimation
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(window?.attributes)
        window?.attributes = layoutParams
    }
}