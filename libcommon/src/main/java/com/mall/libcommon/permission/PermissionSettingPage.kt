package com.mall.libcommon.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.mall.libcommon.ext.areActivityIntent

/**
 * 根据传入的权限自动选择最合适的权限设置页
 */
fun Context.getSmartPermissionIntent(deniedPermissions: List<String>): Intent {
    // 如果失败的权限里面不包含特殊权限
    if (deniedPermissions.isNullOrEmpty() || !containsSpecialPermission(deniedPermissions)){
        return getApplicationDetailsIntent()
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && deniedPermissions.size == 3 &&
        (deniedPermissions.contains(Permission.MANAGE_EXTERNAL_STORAGE) && deniedPermissions.contains(Permission.READ_EXTERNAL_STORAGE)
                && deniedPermissions.contains(Permission.WRITE_EXTERNAL_STORAGE))){
        return getStoragePermissionIntent()
    }

    // 如果当前只有一个权限被拒绝了
    if (deniedPermissions.size == 1){
        val permission = deniedPermissions.first()
        if (Permission.MANAGE_EXTERNAL_STORAGE == permission){
            return getStoragePermissionIntent()
        }
        if (Permission.REQUEST_INSTALL_PACKAGES == permission){
            return getInstallPermissionIntent()
        }
        if (Permission.SYSTEM_ALERT_WINDOW == permission){
            return getWindowPermissionIntent()
        }
        if (Permission.NOTIFICATION_SERVICE == permission){
            return getNotifyPermissionIntent()
        }
        if (Permission.WRITE_SETTINGS == permission){
            return getSettingPermissionIntent()
        }
    }

    return getApplicationDetailsIntent()
}

/**
 * 获取应用详情界面意图
 */
fun Context.getApplicationDetailsIntent(): Intent =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = getPackageNameUri()
    }

/**
 *获取安装权限设置界面意图
 */
fun Context.getInstallPermissionIntent(): Intent {
    var intent: Intent? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = getPackageNameUri()
        }
    }
    if (intent == null || !areActivityIntent(intent)) {
        intent = getApplicationDetailsIntent()
    }
    return intent
}

/**
 * 获取悬浮窗权限设置界面意图
 */
fun Context.getWindowPermissionIntent(): Intent {
    var intent: Intent? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            // 在 Android 11 加包名跳转也是没有效果的，官方文档链接：
            // https://developer.android.google.cn/reference/android/provider/Settings#ACTION_MANAGE_OVERLAY_PERMISSION
            data = getPackageNameUri()
        }
    }
    if (intent == null || !areActivityIntent(intent)) {
        intent = getApplicationDetailsIntent()
    }
    return intent
}

/**
 * 获取通知栏权限设置界面意图
 */
fun Context.getNotifyPermissionIntent(): Intent {
    var intent: Intent? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
//            putExtra(Settings.EXTRA_CHANNEL_ID,applicationInfo.uid)
        }
    }
    if (intent == null || !areActivityIntent(intent)) {
        intent = getApplicationDetailsIntent()
    }
    return intent
}

/**
 * 获取系统设置权限界面意图
 */
fun Context.getSettingPermissionIntent(): Intent {
    var intent: Intent? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = getPackageNameUri()
        }
    }
    if (intent == null || !areActivityIntent(intent)) {
        intent = getApplicationDetailsIntent()
    }
    return intent
}

/**
 * 权限设置页配置
 */
fun Context.getStoragePermissionIntent(): Intent {
    var intent: Intent? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = getPackageNameUri()
        }
    }

    if (intent == null || !areActivityIntent(intent)) {
        intent = getApplicationDetailsIntent()
    }
    return intent
}

/**
 * 获取包名 Uri 对象
 */
fun Context.getPackageNameUri(): Uri = Uri.parse("package:$packageName")