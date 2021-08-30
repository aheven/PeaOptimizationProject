package com.mall.libcommon.ext

import android.R.attr
import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import com.mall.libcommon.base.application


/**
 * 资源获取扩展
 */

/**
 * 获取资源string
 */
fun getResourceString(@StringRes id: Int): String? {
    return getResource()?.getString(id)
}

/**
 * 获取资源对象
 */
fun getResource(): Resources? {
    return application.resources
}

/**
 * 获取当前应用 Apk 在 AssetManager 中的 Cookie
 */
fun Context.findApkPathCookie(): Int {
    return try {
        try {
            // 为什么不直接通过反射 AssetManager.findCookieForPath 方法来判断？因为这个 API 属于反射黑名单，反射执行不了
            val method = assets::class.java.getDeclaredMethod("addOverlayPath", String::class.java)
            method.invoke(assets, applicationInfo.sourceDir) as Int
        } catch (e: Exception) {
            e.printStackTrace()
            val method = assets::class.java.getDeclaredMethod("getApkPaths")
            val apkPaths: Array<String>? = method.invoke(assets) as? Array<String>
            var result = 0
            if (apkPaths != null) {
                for (i in apkPaths.indices) {
                    if (apkPaths[i].equals(attr.path)) {
                        result = i + 1
                        break
                    }
                }
            }
            result
        }
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}