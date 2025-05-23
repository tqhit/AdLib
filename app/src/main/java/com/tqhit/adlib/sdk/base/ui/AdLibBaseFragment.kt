package com.tqhit.adlib.sdk.base.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class AdLibBaseFragment<T: ViewBinding> : Fragment() {
    abstract val binding : T

    protected open fun setupData() {}
    protected open fun setupListener() {}
    protected open fun setupUI() {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupData()
        setupUI()
        setupListener()
    }
}