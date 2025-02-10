package com.tqhit.adlib.sdk.base.ui

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class AdLibBaseActivity<T: ViewDataBinding> : AppCompatActivity() {
    protected lateinit var binding: T

    @get:LayoutRes
    protected abstract val layout: Int

    protected open fun setupData() {}
    protected open fun setupListener() {}
    protected open fun setupUI() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        setupData()
        setupUI()
        setupListener()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this, layout)
        binding.lifecycleOwner = this
    }

    protected fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}