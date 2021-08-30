package com.mall.libcommon.interceptor

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import com.mall.libcommon.ext.toast
import com.mall.libcommon.permission.IPermissionInterceptor
import com.mall.libcommon.permission.OnPermissionCallback
import com.mall.libcommon.permission.Permission
import com.mall.libcommon.permission.PermissionsUtil

class PermissionInterceptor : IPermissionInterceptor {
    override fun deniedPermissions(
        activity: Activity,
        callback: OnPermissionCallback,
        permissions: List<String>,
        never: Boolean
    ) {
        if (never) {
            showPermissionDialog(activity, permissions)
            return
        }

        if (permissions.size == 1 && Permission.ACCESS_BACKGROUND_LOCATION == permissions.first()) {
            toast("没有授予后台定位权限，请您选择始终允许")
            return
        }

        toast("授权失败，请正确授予权限")

        callback.onDenied(permissions, never)
    }

    private fun showPermissionDialog(activity: Activity, permissions: List<String>) {
        AlertDialog.Builder(activity)
            .setTitle("授权提醒")
            .setCancelable(false)
            .setMessage(getPermissionHint(activity, permissions))
            .setPositiveButton("前往授权") { dialog, _ ->
                dialog.dismiss()
                PermissionsUtil.startPermissionActivity(activity, permissions)
            }.show()
    }

    /**
     * 根据权限获取提示
     */
    private fun getPermissionHint(context: Context, permissions: List<String>): String {
        if (permissions.isNullOrEmpty()) {
            return "获取权限失败，请手动授权"
        }
        val hints = mutableListOf<String>()
        for (permission in permissions) {
            when (permission) {
                Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE, Permission.MANAGE_EXTERNAL_STORAGE -> {
                    addHint(hints, "存储权限")
                }
                Permission.CAMERA -> {
                    addHint(hints, "相机权限")
                }
                Permission.RECORD_AUDIO -> {
                    addHint(hints, "麦克风权限")
                }
                Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION, Permission.ACCESS_BACKGROUND_LOCATION -> {
                    if (!permissions.contains(Permission.ACCESS_FINE_LOCATION) && !permissions.contains(
                            Permission.ACCESS_COARSE_LOCATION
                        )
                    ) {
                        addHint(hints, "后台定位权限")
                    } else {
                        addHint(hints, "定位权限")
                    }
                }
                Permission.READ_PHONE_STATE, Permission.CALL_PHONE, Permission.ADD_VOICEMAIL, Permission.USE_SIP, Permission.READ_PHONE_NUMBERS, Permission.ANSWER_PHONE_CALLS -> {
                    addHint(hints, "电话权限")
                }
                Permission.GET_ACCOUNTS, Permission.READ_CONTACTS, Permission.WRITE_CONTACTS -> {
                    addHint(hints, "通讯录权限")
                }
                Permission.READ_CALENDAR, Permission.WRITE_CALENDAR -> {
                    addHint(hints, "日历权限")
                }
                Permission.READ_CALL_LOG, Permission.WRITE_CALL_LOG, Permission.PROCESS_OUTGOING_CALLS -> {
                    addHint(
                        hints,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "通话记录权限" else "电话权限"
                    )
                }
                Permission.BODY_SENSORS -> {
                    addHint(hints, "身体传感器权限")
                }
                Permission.ACTIVITY_RECOGNITION -> {
                    addHint(hints, "健身运动权限")
                }
                Permission.SEND_SMS, Permission.RECEIVE_SMS, Permission.READ_SMS, Permission.RECEIVE_WAP_PUSH, Permission.RECEIVE_MMS -> {
                    addHint(hints, "短信权限")
                }
                Permission.REQUEST_INSTALL_PACKAGES -> {
                    addHint(hints, "安装应用权限")
                }
                Permission.NOTIFICATION_SERVICE -> {
                    addHint(hints, "通知栏权限")
                }
                Permission.SYSTEM_ALERT_WINDOW -> {
                    addHint(hints, "悬浮窗权限")
                }
                Permission.WRITE_SETTINGS -> {
                    addHint(hints, "系统设置权限")
                }
            }
        }
        if (!hints.isNullOrEmpty()) {
            val builder = StringBuilder()
            for (text in hints) {
                if (builder.isEmpty()) {
                    builder.append(text)
                } else {
                    builder.append("、")
                        .append(text)
                }
            }
            builder.append(" ")
            return "获取权限失败，请手动授权：${builder}"
        }
        return "获取权限失败，请手动授权"
    }

    private fun addHint(hints: MutableList<String>, message: String) {
        if (!hints.contains(message)) {
            hints.add(message)
        }
    }
}