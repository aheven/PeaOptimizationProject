package com.mall.libcommon.base

import android.app.Application
import androidx.multidex.MultiDexApplication
import com.alibaba.android.arouter.launcher.ARouter
import com.mall.libcommon.BuildConfig
import com.mall.libcommon.lifecycle.ActivityLifecycleCallback

lateinit var application:Application

open class BaseApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        application = this
        registerActivityLifecycleCallbacks(ActivityLifecycleCallback())
        initThreadApp()

    }

    private fun initThreadApp() {
        if (BuildConfig.DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(this)
    }
}