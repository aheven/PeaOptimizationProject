package com.mall.libcommon.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    protected lateinit var binding: VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return reflexViewBinding(inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    @Suppress("UNCHECKED_CAST")
    private fun reflexViewBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val entityClass =
            (this::class.java.genericSuperclass as ParameterizedType).actualTypeArguments.first()
        val declaredMethod = (entityClass as Class<VB>).getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
        binding = declaredMethod.invoke(this, inflater, container, false) as VB
        return binding.root
    }

    abstract fun initView()

    abstract fun initData()
}