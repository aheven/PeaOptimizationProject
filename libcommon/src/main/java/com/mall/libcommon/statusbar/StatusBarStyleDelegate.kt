package com.mall.libcommon.statusbar

import android.app.Activity
import android.graphics.Color
import com.mall.libcommon.ext.applyDarkMode
import com.mall.libcommon.ext.setStatusBarColor
import com.mall.libcommon.ext.setStatusBarFullTransparent
import kotlin.reflect.KProperty

class StatusBarStyleDelegate {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): StatusBarStyle {
        return StatusBarStyle.WHITE
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: StatusBarStyle) {
        val activity = thisRef as Activity
        activity.applyDarkMode()
        when (value) {
            StatusBarStyle.TRANSLATE -> {
                activity.setStatusBarFullTransparent()
            }
            StatusBarStyle.WHITE -> {//rebate包与主包一致
                activity.setStatusBarColor(Color.WHITE)
            }
        }
    }
}