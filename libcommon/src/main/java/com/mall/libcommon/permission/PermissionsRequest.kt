package com.mall.libcommon.permission

import android.app.Activity
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import java.lang.reflect.Method
import java.util.*


/**
 * 判断某个权限是否被赋予
 */
fun Context.isGrantedPermission(permission: String): Boolean {
    // 如果是安卓 6.0 以下版本就默认授予
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
    // 检测存储权限
    if (Permission.MANAGE_EXTERNAL_STORAGE == permission) {
        return isGrantedStoragePermission()
    }
    //检测安装权限
    if (Permission.REQUEST_INSTALL_PACKAGES == permission) {
        return isGrantedInstallPermission()
    }
    // 检测悬浮窗权限
    if (Permission.SYSTEM_ALERT_WINDOW == permission) {
        return isGrantedWindowPermission()
    }
    // 检测通知栏权限
    if (Permission.NOTIFICATION_SERVICE == permission) {
        return isGrantedNotifyPermission()
    }
    // 检测系统权限
    if (Permission.WRITE_SETTINGS == permission) {
        return isGrantedSettingPermission()
    }
    // 检测 10.0 的三个新权限
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        if (Permission.ACCESS_BACKGROUND_LOCATION == permission) {
            return checkSelfPermission(Permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

        if (Permission.ACTIVITY_RECOGNITION == permission) {
            return checkSelfPermission(Permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED
        }

        if (Permission.ACCESS_MEDIA_LOCATION == permission) {
            return true
        }
    }
    // 检测 9.0 的一个新权限
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        if (Permission.ACCEPT_HANDOVER == permission) {
            return true
        }
    }
    // 检测 8.0 的两个新权限
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        if (Permission.ANSWER_PHONE_CALLS == permission) {
            return true
        }
        if (Permission.READ_PHONE_NUMBERS == permission) {
            return checkSelfPermission(Permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}

/**
 * 是否有存储权限
 */
fun Context.isGrantedStoragePermission(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        return Environment.isExternalStorageManager()
    }
    return isGrantedPermissions(Permission.STORAGE.toList())
}

/**
 * 是否有安装权限
 */
fun Context.isGrantedInstallPermission(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return packageManager.canRequestPackageInstalls()
    }
    return true
}

/**
 * 是否有悬浮窗权限
 */
fun Context.isGrantedWindowPermission(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return Settings.canDrawOverlays(this)
    }
    return true
}

/**
 * 是否有通知栏权限
 */
fun Context.isGrantedNotifyPermission(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return getSystemService(NotificationManager::class.java).areNotificationsEnabled()
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // 参考 Support 库中的方法： NotificationManagerCompat.from(context).areNotificationsEnabled()
        val appOps = getSystemService(Context.APP_OPS_SERVICE)
        try {
            val method: Method = appOps::class.java.getMethod(
                "checkOpNoThrow",
                Integer.TYPE,
                Integer.TYPE,
                String::class.java
            )
            val field = appOps::class.java.getDeclaredField("OP_POST_NOTIFICATION")
            val value = field[Int::class.java] as Int
            return method.invoke(
                appOps,
                value,
                applicationInfo.uid,
                packageName
            ) as Int == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }
    }
    return true
}

/**
 * 是否有系统设置权限
 */
fun Context.isGrantedSettingPermission(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return Settings.System.canWrite(this)
    }
    return true
}

/**
 * 检查某些权限是否被全部赋予
 */
fun Context.isGrantedPermissions(permissions: List<String>): Boolean {
    //如果是Android 6.0 以下版本则直接返回
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true

    if (permissions.isNullOrEmpty()) return false

    for (permission in permissions) {
        if (!isGrantedPermission(permission)) return false
    }
    return true
}

/**
 * 判断某个权限集合是否包含特殊权限
 */
fun containsSpecialPermission(permissions: List<String>?): Boolean {
    if (permissions.isNullOrEmpty()) {
        return false
    }
    for (permission in permissions) {
        if (isSpecialPermission(permission)) {
            return true
        }
    }
    return false
}

/**
 * 判断某个权限是否是特殊权限
 */
fun isSpecialPermission(permission: String): Boolean {
    return Permission.MANAGE_EXTERNAL_STORAGE == permission || Permission.REQUEST_INSTALL_PACKAGES == permission || Permission.SYSTEM_ALERT_WINDOW == permission || Permission.NOTIFICATION_SERVICE == permission || Permission.WRITE_SETTINGS == permission
}

/**
 *获取某个权限的状态
 */
fun Context.getPermissionStatus(permission: String): Int {
    return if (isGrantedPermission(permission)) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
}

/**
 * 判断某个权限是否被永久拒绝
 */
fun Activity.isPermissionPermanentDenied(permission: String): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false

    // 特殊权限不算，本身申请方式和危险权限申请方式不同，因为没有永久拒绝的选项，所以这里返回 false
    if (isSpecialPermission(permission)) return false

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // 重新检测后台定位权限是否永久拒绝
        if (Permission.ACCESS_BACKGROUND_LOCATION == permission && !isGrantedPermission(Permission.ACCESS_BACKGROUND_LOCATION)
            && !isGrantedPermission(Permission.ACCESS_FINE_LOCATION)
        ) {
            return !shouldShowRequestPermissionRationale(Permission.ACCESS_FINE_LOCATION)
        }
    }

    // 检测 10.0 的三个新权限
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        if (Permission.ACCESS_BACKGROUND_LOCATION == permission) {
            return !isGrantedPermission(Permission.ACCESS_FINE_LOCATION) &&
                    !shouldShowRequestPermissionRationale(Permission.ACCESS_FINE_LOCATION)
        }

        if (Permission.ACTIVITY_RECOGNITION == permission) {
            return !isGrantedPermission(Permission.BODY_SENSORS) &&
                    !shouldShowRequestPermissionRationale(Permission.BODY_SENSORS)
        }

        if (Permission.ACCESS_MEDIA_LOCATION == permission) {
            return false
        }
    }

    // 检测 9.0 的一个新权限
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        if (Permission.ACCEPT_HANDOVER == permission)
            return false
    }

    // 检测 8.0 的两个新权限
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        if (Permission.ANSWER_PHONE_CALLS == permission)
            return true
        if (Permission.READ_PHONE_NUMBERS == permission) {
            return !isGrantedPermission(Permission.READ_PHONE_STATE) &&
                    !shouldShowRequestPermissionRationale(Permission.READ_PHONE_STATE)
        }
    }

    return !isGrantedPermission(permission) &&
            !shouldShowRequestPermissionRationale(permission)
}

/**
 * 在权限组中检查是否有某个权限是否被永久拒绝
 */
fun Activity.isPermissionPermanentDenied(permissions: List<String>): Boolean {
    for (permission in permissions) {
        if (isPermissionPermanentDenied(permission)) return true
    }
    return false
}

/**
 *获取已授予的权限
 */
fun getGrantedPermissions(permissions: Array<out String>, grantResults: IntArray): List<String> {
    val grantedPermissions: MutableList<String> = ArrayList()
    for (i in grantResults.indices) {
        // 把授予过的权限加入到集合中
        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
            grantedPermissions.add(permissions[i])
        }
    }
    return grantedPermissions
}

/**
 *获取被拒绝的权限
 */
fun getDeniedPermissions(permissions: Array<out String>, grantResults: IntArray): List<String> {
    val deniedPermissions: MutableList<String> = ArrayList()
    for (i in grantResults.indices) {
        // 把没有授予过的权限加入到集合中
        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
            deniedPermissions.add(permissions[i])
        }
    }
    return deniedPermissions
}