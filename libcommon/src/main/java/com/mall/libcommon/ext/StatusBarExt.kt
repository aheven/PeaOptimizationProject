package com.mall.libcommon.ext

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat

/**
 * 关于状态栏相关的扩展函数
 **/

/**
 * 设置状态栏全透明
 */
fun Activity.setStatusBarFullTransparent() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT

    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }
}

/**
 * 设置状态栏颜色
 */
fun Activity.setStatusBarColor(@ColorInt color: Int, @IntRange(from = 0, to = 255) alpha: Int = 0) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.run {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = cipherColor(color, alpha)
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        window.run {
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            setTranslucentView(window.decorView as ViewGroup, color, alpha)
            setRootView(true)
        }
    }
}

/**
 * 设置根布局参数
 */
private fun Activity.setRootView(fitSystemWindow: Boolean) {
    val parent = findViewById<ViewGroup>(android.R.id.content)
    (0 until parent.childCount).forEach { index ->
        val childView = parent.getChildAt(index)
        if (childView is ViewGroup) {
            childView.fitsSystemWindows = fitSystemWindow
            childView.clipToPadding = fitSystemWindow
        }
    }
}

/**
 * 创建透明View
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
private fun setTranslucentView(viewGroup: ViewGroup, color: Int, alpha: Int) {
    val cipherColor = cipherColor(color, alpha)
    var translucentView = viewGroup.findViewById<View>(android.R.id.custom)
    if (translucentView == null && cipherColor != 0) {
        translucentView = View(viewGroup.context)
        translucentView.id = android.R.id.custom
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            viewGroup.context.resources.getStatusBarHeight()
        )
        viewGroup.addView(translucentView, params)
    }
    translucentView?.setBackgroundColor(cipherColor)
}

private fun Resources.getStatusBarHeight(): Int {
    val resourceId = getIdentifier("status_bar_height", "dimen", "android")
    return getDimensionPixelSize(resourceId)
}

/**
 * 计算alpha色值
 * @param color 状态栏颜色值
 * @param alpha 状态栏透明度
 */
private fun cipherColor(@ColorInt color: Int, alpha: Int): Int {
    if (alpha == 0) {
        return color
    }
    val a = 1 - alpha / 255f
    var red = color shr 16 and 0xff
    var green = color shr 8 and 0xff
    var blue = color and 0xff
    red = (red * a + 0.5).toInt()
    green = (green * a + 0.5).toInt()
    blue = (blue * a + 0.5).toInt()
    return 0xff shl 24 or (red shl 16) or (green shl 8) or blue
}

/**
 *设置状态栏darkMode,字体颜色及icon变黑(目前支持MIUI6以上,Flyme4以上,Android M以上)
 */
fun Activity.applyDarkMode() {
    window.darkMode(true)
}

@TargetApi(Build.VERSION_CODES.M)
private fun Window.darkMode(dark: Boolean) {
    when {
        isFlyme4() -> {
            setModeForFlyme4(dark)
        }
        isMIUI6() -> {
            setModeForMIUI6(dark)
        }
        else -> {
            darkModeForM(dark)
        }
    }
}

/**
 * android 6.0设置字体颜色
 *
 * @param dark   亮色 or 暗色
 */
@RequiresApi(Build.VERSION_CODES.M)
private fun Window.darkModeForM(dark: Boolean) {
    val controller =
        WindowCompat.getInsetsController(this, decorView.findViewById(android.R.id.content))
    controller?.isAppearanceLightStatusBars = dark
}

/**
 * 设置Flyme4+的状态栏的darkMode,darkMode时候字体颜色及icon
 * http://open-wiki.flyme.cn/index.php?title=Flyme%E7%B3%BB%E7%BB%9FAPI
 *
 * @param dark   亮色 or 暗色
 */
private fun Window.setModeForFlyme4(dark: Boolean) {
    try {
        val lp = attributes
        val darkFlag =
            WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
        val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
        darkFlag.isAccessible = true
        meizuFlags.isAccessible = true
        val bit = darkFlag.getInt(null)
        var value = meizuFlags.getInt(lp)
        value = if (dark) {
            value or bit
        } else {
            value and bit.inv()
        }
        meizuFlags.setInt(lp, value)
        attributes = lp
    } catch (_: Exception) {
        loge("set flyme4 dark icon failed.")
    }
}

/**
 * 设置MIUI6+的状态栏的darkMode,darkMode时候字体颜色及icon
 * http://dev.xiaomi.com/doc/p=4769/
 *
 * @param dark   亮色 or 暗色
 */
@SuppressLint("PrivateApi")
private fun Window.setModeForMIUI6(dark: Boolean) {
    val clazz = Window::class.java
    try {
        val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
        val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
        val darkModeFlag = field.getInt(layoutParams)
        val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.java, Int::class.java)
        extraFlagField.invoke(this, if (dark) 0 else darkModeFlag)
    } catch (_: Exception) {
        loge("set miui6 dark icon failed.")
    }
}