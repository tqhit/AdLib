package com.tqhit.adlib.sdk.base.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class AdLibBaseFragment<T: ViewDataBinding> : Fragment() {
    protected lateinit var binding: T

    @get:LayoutRes
    protected abstract val layout: Int

    protected open fun setupData() {}
    protected open fun setupListener() {}
    protected open fun setupUI() {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<T>(inflater, layout, container, false).also {
            it.lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupData()
        setupUI()
        setupListener()
    }
}