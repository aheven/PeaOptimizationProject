package com.mall.libcommon.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
import android.os.Build
import com.mall.libcommon.ext.findApkPathCookie
import com.mall.libcommon.ext.isActive


/**
 * 检测Activity的状态是否正常
 */
fun checkActivityStatus(activity: Activity?, debugMode: Boolean): Boolean {
    // 检查当前 Activity 状态是否是正常的，如果不是则不请求权限
    if (!activity.isActive()) {
        if (debugMode) {
            // Context 的实例必须是 Activity 对象
            // 这个 Activity 对象当前不能是关闭状态，这种情况常出现在执行异步请求后申请权限，请自行在外层判断 Activity 状态是否正常之后再进入权限申请
            // 这个 Activity 对象当前不能是销毁状态，这种情况常出现在执行异步请求后申请权限，请自行在外层判断 Activity 状态是否正常之后再进入权限申请
            throw IllegalArgumentException(
                "The instance of the Context must be an Activity object |" +
                        "The Activity has been finishing, Please manually determine the status of the Activity |" +
                        "The Activity has been destroyed, Please manually determine the status of the Activity"
            )
        }
        return false
    }
    return true
}

/**
 * 检测传入的权限是否符合要求
 */
fun checkPermissionArgument(requestPermissions: List<String>, debugMode: Boolean): Boolean {
    if (requestPermissions.isEmpty()) {
        if (debugMode) {
            throw IllegalArgumentException("The requested permission cannot be empty")
        }
        return false
    }
    if (debugMode) {
        val allPermissions = mutableListOf<String>()
        val fields = Permission::class.java.declaredFields
        // 在开启代码混淆之后，反射 Permission 类中的字段会得到空的字段数组
        // 这个是因为编译后常量会在代码中直接引用，所以 Permission 常量字段在混淆的时候会被移除掉
        if (fields.isNullOrEmpty()) return true
        for (field in fields) {
            if (String::class.java != field.type) continue
            allPermissions.add(field.get(null) as String)
        }

        for (permission in requestPermissions) {
            // 请不要申请危险权限和特殊权限之外的权限
            if (!allPermissions.contains(permission))
                throw  IllegalArgumentException("The $permission is not a dangerous permission or special permission");
        }
    }
    return true
}

/**
 * 检查存储权限
 */
fun Context.checkStoragePermission(requestPermissions: List<String>) {
    // 如果请求的权限中没有包含外部存储相关的权限，那么就直接返回
    if (!requestPermissions.contains(Permission.MANAGE_EXTERNAL_STORAGE) &&
        !requestPermissions.contains(Permission.READ_EXTERNAL_STORAGE) &&
        !requestPermissions.contains(Permission.WRITE_EXTERNAL_STORAGE)
    ) return

    val cookie = findApkPathCookie()

    val parser = assets.openXmlResourceParser(cookie, "AndroidManifest.xml")
    while (parser.eventType != XmlResourceParser.END_DOCUMENT) {
        // 当前节点是否为标签头部
        if (parser.eventType == XmlResourceParser.START_TAG) {
            val nodeName = parser.name
            if ("application" == nodeName) {
                val namespace = "http://schemas.android.com/apk/res/android"
                val targetSdkVersion = applicationInfo.targetSdkVersion
                val requestLegacyExternalStorage = parser.getAttributeBooleanValue(
                    namespace,
                    "requestLegacyExternalStorage",
                    false
                )
                // 如果在已经适配 Android 10 的情况下
                if (targetSdkVersion >= Build.VERSION_CODES.Q && !requestLegacyExternalStorage
                    && requestPermissions.contains(Permission.MANAGE_EXTERNAL_STORAGE)
                ) {
                    // 请在清单文件 Application 节点中注册 android:requestLegacyExternalStorage="true" 属性
                    // 否则就算申请了权限，也无法在 Android 10 的设备上正常读写外部存储上的文件
                    // 如果你的项目已经全面适配了分区存储，请调用 XXPermissions.setScopedStorage(true) 来跳过该检查
                    throw IllegalStateException("Please register the android:requestLegacyExternalStorage=\"true\" attribute in the manifest file")
                }
                // 如果在已经适配 Android 11 的情况下
                if (targetSdkVersion >= Build.VERSION_CODES.R &&
                    !requestPermissions.contains(Permission.MANAGE_EXTERNAL_STORAGE)
                ) {
                    // 1. 适配分区存储的特性，并在 Application 初始化时调用 XXPermissions.setScopedStorage(true)
                    // 2. 如果不想适配分区存储，则需要使用 Permission.MANAGE_EXTERNAL_STORAGE 来申请权限
                    // 上面两种方式需要二选一，否则无法在 Android 11 的设备上正常读写外部存储上的文件
                    // 如果不知道该怎么选择，可以看文档：https://github.com/getActivity/XXPermissions/blob/master/HelpDoc
                    throw IllegalArgumentException("Please adapt the scoped storage, or use the MANAGE_EXTERNAL_STORAGE permission")
                }
                break
            }
        }
        parser.next()
    }
    parser.close()
}

/**
 * 检查定位权限
 */
fun checkLocationPermission(requestPermissions: List<String>) {
    // 判断是否包含后台定位权限
    if (!requestPermissions.contains(Permission.ACCESS_BACKGROUND_LOCATION)) return

    if (requestPermissions.contains(Permission.ACCESS_COARSE_LOCATION) &&
        !requestPermissions.contains(Permission.ACCESS_FINE_LOCATION)
    ) {
        // 申请后台定位权限可以不包含模糊定位权限，但是一定要包含精确定位权限，否则后台定位权限会无法申请，也就是会导致无法弹出授权弹窗
        throw IllegalArgumentException("The application for background location permissions must include precise location permissions");
    }

    for (permission in requestPermissions) {
        if (Permission.ACCESS_FINE_LOCATION == permission || Permission.ACCESS_COARSE_LOCATION == permission
            || Permission.ACCESS_BACKGROUND_LOCATION == permission
        ) continue

        // 因为包含了后台定位权限，所以请不要申请和定位无关的权限，因为在 Android 11 上面，后台定位权限不能和其他非定位的权限一起申请
        // 否则会出现只申请了后台定位权限，其他权限会被回绝掉的情况，因为在 Android 11 上面，后台定位权限是要跳 Activity，并非弹 Dialog
        // 如果你的项目没有后台定位的需求，请不要一同申请 Permission.ACCESS_BACKGROUND_LOCATION 权限
        throw java.lang.IllegalArgumentException("Because it includes background location permissions, do not apply for permissions unrelated to location")
    }
}

/**
 * 检测 targetSdk 是否符合要求
 */
fun checkTargetSdkVersion(context: Context, requestPermissions: List<String>) {
    val targetSdkMinVersion: Int =
        if (requestPermissions.contains(Permission.MANAGE_EXTERNAL_STORAGE))
        // 必须设置 targetSdkVersion >= 30 才能正常检测权限，否则请使用 Permission.Group.STORAGE 来申请存储权限
            Build.VERSION_CODES.R
        else if (requestPermissions.contains(Permission.ACCEPT_HANDOVER))
            Build.VERSION_CODES.P
        else if (requestPermissions.contains(Permission.ACCESS_BACKGROUND_LOCATION) ||
            requestPermissions.contains(Permission.ACTIVITY_RECOGNITION) ||
            requestPermissions.contains(Permission.ACCESS_MEDIA_LOCATION)
        )
            Build.VERSION_CODES.Q
        else if (requestPermissions.contains(Permission.REQUEST_INSTALL_PACKAGES) ||
            requestPermissions.contains(Permission.ANSWER_PHONE_CALLS) ||
            requestPermissions.contains(Permission.READ_PHONE_NUMBERS)
        )
            Build.VERSION_CODES.O
        else {
            Build.VERSION_CODES.M
        }
    // 必须设置正确的 targetSdkVersion 才能正常检测权限
    if (context.applicationInfo.targetSdkVersion < targetSdkMinVersion)
        throw RuntimeException("The targetSdkVersion SDK must be $targetSdkMinVersion or more")
}

/**
 * 处理和优化已经过时的权限
 */
fun optimizeDeprecatedPermission(requestPermissions: MutableList<String>) {
    // 如果本次申请包含了 Android 11 存储权限
    if (requestPermissions.contains(Permission.MANAGE_EXTERNAL_STORAGE)) {
        if (requestPermissions.contains(Permission.READ_EXTERNAL_STORAGE) ||
            requestPermissions.contains(Permission.WRITE_EXTERNAL_STORAGE)
        ) {
            // 检测是否有旧版的存储权限，有的话直接抛出异常，请不要自己动态申请这两个权限
            throw  IllegalArgumentException(
                "If you have applied for MANAGE_EXTERNAL_STORAGE permissions, " +
                        "do not apply for the READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE permissions"
            )
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            // 自动添加旧版的存储权限，因为旧版的系统不支持申请新版的存储权限
            requestPermissions.add(Permission.READ_EXTERNAL_STORAGE);
            requestPermissions.add(Permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.O) &&
        requestPermissions.contains(Permission.READ_PHONE_NUMBERS) &&
        !requestPermissions.contains(Permission.READ_PHONE_STATE)
    ) {
        // 自动添加旧版的读取电话号码权限，因为旧版的系统不支持申请新版的权限
        requestPermissions.add(Permission.READ_PHONE_STATE)
    }

    if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) &&
        requestPermissions.contains(Permission.ACTIVITY_RECOGNITION) &&
        !requestPermissions.contains(Permission.BODY_SENSORS)
    ) {
        // 自动添加传感器权限，因为这个权限是从 Android 10 开始才从传感器权限中剥离成独立权限
        requestPermissions.add(Permission.BODY_SENSORS)
    }
}

/**
 * 返回应用程序在清单文件中注册的权限
 */
fun Context.getManifestPermissions(): List<String>? =
    try {
        val requestedPermissions = packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_PERMISSIONS
        ).requestedPermissions
        requestedPermissions.toList()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

/**
 * 检测权限有没有在清单文件中注册
 */
fun Context.checkPermissionManifest(requestPermissions: List<String>) {
    val manifestPermissions = getManifestPermissions()
    if (manifestPermissions.isNullOrEmpty()) throw RuntimeException("No permissions are registered in the manifest file")
    val minSdkVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        applicationInfo.minSdkVersion
    } else {
        Build.VERSION_CODES.M
    }

    for (permission in requestPermissions) {
        if (minSdkVersion < Build.VERSION_CODES.R) {
            if (Permission.MANAGE_EXTERNAL_STORAGE == permission) {
                if (!manifestPermissions.contains(Permission.READ_EXTERNAL_STORAGE)) {
                    // 为了保证能够在旧版的系统上正常运行，必须要在清单文件中注册此权限
                    throw IllegalArgumentException("Permission.READ_EXTERNAL_STORAGE : Permissions are not registered in the manifest file")
                }

                if (!manifestPermissions.contains(Permission.WRITE_EXTERNAL_STORAGE)) {
                    // 为了保证能够在旧版的系统上正常运行，必须要在清单文件中注册此权限
                    throw IllegalArgumentException("Permission.WRITE_EXTERNAL_STORAGE : Permissions are not registered in the manifest file")
                }
            }
        }

        if (minSdkVersion < Build.VERSION_CODES.Q) {
            if (Permission.ACTIVITY_RECOGNITION == permission && !manifestPermissions.contains(
                    Permission.BODY_SENSORS
                )
            ) {
                // 为了保证能够在旧版的系统上正常运行，必须要在清单文件中注册此权限
                throw IllegalArgumentException("Permission.BODY_SENSORS : Permissions are not registered in the manifest file")
            }
        }

        if (minSdkVersion < Build.VERSION_CODES.O) {
            if (Permission.READ_PHONE_NUMBERS == permission && !manifestPermissions.contains(
                    Permission.READ_PHONE_STATE
                )
            ) {
                // 为了保证能够在旧版的系统上正常运行，必须要在清单文件中注册此权限
                throw IllegalArgumentException("Permission.READ_PHONE_STATE : Permissions are not registered in the manifest file")
            }
        }

        //不检测通知栏权限有没有在清单文件中注册，因为这个权限是框架虚拟出来的，有没有在清单文件中注册都没关系
        if (Permission.NOTIFICATION_SERVICE == permission) continue

        if (!manifestPermissions.contains(permission)) throw RuntimeException("$permission are not registered in the manifest file")
    }
}