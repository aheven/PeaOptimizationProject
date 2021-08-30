package com.mall.libcommon.permission

import android.app.Activity

/**
 * 权限请求拦截器
 */
interface IPermissionInterceptor {
    /**
     * 权限申请拦截，可在此处先弹 Dialog 再申请权限
     */
    fun requestPermissions(
        activity: Activity,
        callback: OnPermissionCallback,
        permissions: List<String>
    ) {
        PermissionFragment.beginRequest(activity, permissions, this, callback)
    }

    /**
     * 权限授予回调拦截，参见 {@link OnPermissionCallback#onGranted(List, boolean)}
     */
    fun grantedPermissions(
        activity: Activity,
        callback: OnPermissionCallback,
        permissions: List<String>,
        all: Boolean
    ) {
        callback.onGranted(permissions, all)
    }

    /**
     * 权限拒绝回调拦截，参见 {@link OnPermissionCallback#onDenied(List, boolean)}
     */
    fun deniedPermissions(
        activity: Activity,
        callback: OnPermissionCallback,
        permissions: List<String>,
        never: Boolean
    ) {
        callback.onDenied(permissions, never)
    }
}