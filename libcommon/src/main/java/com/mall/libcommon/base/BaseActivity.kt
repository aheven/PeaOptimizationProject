package com.mall.libcommon.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.mall.libcommon.statusbar.StatusBarStyle
import com.mall.libcommon.statusbar.StatusBarStyleDelegate
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: VB

    private var statusBarStyle : StatusBarStyle by StatusBarStyleDelegate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reflexViewBinding()
        setContentView(binding.root)
        statusBarStyle = setCustomStatusBarStyle()

        initView()
        initData()
    }

    open fun setCustomStatusBarStyle(): StatusBarStyle = StatusBarStyle.WHITE

    @Suppress("UNCHECKED_CAST")
    private fun reflexViewBinding() {
        val entityClass =
            (this::class.java.genericSuperclass as ParameterizedType).actualTypeArguments.first()
        val declaredMethod =
            (entityClass as Class<VB>).getDeclaredMethod("inflate", LayoutInflater::class.java)
        binding = declaredMethod.invoke(this, layoutInflater) as VB
    }

    abstract fun initView()

    abstract fun initData()
}