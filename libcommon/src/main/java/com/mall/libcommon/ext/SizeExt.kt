package com.mall.libcommon.ext

import android.content.res.Resources

/**
 * 数字dp转px对象
 */
fun Int.px(): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (this * scale + 0.5f).toInt()
}

/**
 * 数字px转dp对象
 */
fun Int.dp(): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (this / scale + 0.5f).toInt()
}

/**
 * 数字sp转px
 */
fun Int.sp(): Int {
    val fontScale = Resources.getSystem().displayMetrics.scaledDensity
    return (this * fontScale + 0.5f).toInt()
}