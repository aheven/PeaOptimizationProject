package com.mall.libcommon.ext

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.mall.libcommon.lifecycle.activityList

/**
 * 获取最顶层的Activity
 */
fun getTopActivity(): Activity? {
    for (position in activityList.lastIndex downTo 0) {
        val activity = activityList[position]
        if (!activity.isActive()) {
            continue
        }
        return activity
    }
    return null
}

/**
 * 判断Activity是否是活跃状态
 */
fun Activity?.isActive() =
    this != null && !isFinishing && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !isDestroyed)

/**
 * 查找Activity
 */
fun Context.findActivity(): Activity? {
    var current: Context? = this
    do {
        if (current is Activity)
            return current
        else if (current is ContextWrapper)
            current = current.baseContext
        else
            return null
    } while (current != null)
    return null
}

fun Context.areActivityIntent(intent: Intent): Boolean {
    return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        .isNotEmpty()
}