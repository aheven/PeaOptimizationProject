package com.mall.libcommon.ext

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build

/**
 * 系统相关信息获取扩展函数
 */

/**
 *判断是否Flyme4以上系统
 */
fun isFlyme4() = Build.FINGERPRINT.contains("Flyme_OS_4") ||
        Build.VERSION.INCREMENTAL.contains("Flyme_OS_4") ||
        Build.DISPLAY.matches(Regex("Flyme OS [4|5]", RegexOption.IGNORE_CASE))

/**
 * 判断是否是MIUI6以上
 */
@SuppressLint("PrivateApi")
fun isMIUI6() = try {
    val clz = Class.forName("android.os.SystemProperties")
    val mtd = clz.getMethod("get", String::class.java)
    var value: String = mtd.invoke(null, "ro.miui.ui.version.name") as String
    value = value.replace("[vV]".toRegex(), "")
    value.toInt() >= 6
} catch (_: Exception) {
    false
}

/**
 * 根据key获取Application中指定的meta-data
 */
fun Application.getAppMetaData(key: String): String? {
    var metaData: String? = null
    if (packageManager != null) {
        val applicationInfo =
            packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        metaData = applicationInfo.metaData?.getString(key)
    }
    return metaData
}

/**
 * 获取应用版本
 */
fun Application.getAppVersionCode(): Long {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode
    } else {
        packageInfo.versionCode.toLong()
    }
}

/**
 * 获取应用版本名
 */
fun Application.getAppVersionName(): String {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    return packageInfo.versionName
}
