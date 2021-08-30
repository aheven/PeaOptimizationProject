package com.mall.libcommon.arouter

import com.alibaba.android.arouter.launcher.ARouter
import kotlin.reflect.KClass

object Launcher {
    fun <T : Any> navigation(launcher: KClass<T>): T {
        return ARouter.getInstance().navigation(launcher.java)
    }
}