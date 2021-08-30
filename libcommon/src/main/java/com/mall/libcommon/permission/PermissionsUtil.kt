package com.mall.libcommon.permission

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.mall.libcommon.BuildConfig
import com.mall.libcommon.ext.findActivity

class PermissionsUtil private constructor(
    private val context: Context
) {
    companion object {
        const val REQUEST_CODE = 1024 + 1

        /*权限请求拦截器*/
        private var sPermissionInterceptor: IPermissionInterceptor? = null

        fun with(context: Context) = PermissionsUtil(context)

        fun with(fragment: Fragment) = PermissionsUtil(fragment.requireContext())

        fun setInterceptor(interceptor: IPermissionInterceptor) {
            sPermissionInterceptor = interceptor
        }

        fun getInterceptor(): IPermissionInterceptor =
            sPermissionInterceptor ?: synchronized(this) {
                object : IPermissionInterceptor {}.also { sPermissionInterceptor = it }
            }

        /**
         * 跳转到权限设置页
         */
        fun startPermissionContext(context: Context, permission: List<String>) {
            val activity = context.findActivity()
            if (activity != null) {
                startPermissionActivity(activity, permission, REQUEST_CODE)
                return
            }
        }

        fun startPermissionActivity(
            activity: Activity,
            permissions: List<String>
        ) {
            startPermissionActivity(activity, permissions, REQUEST_CODE)
        }

        fun startPermissionActivity(
            activity: Activity,
            permissions: List<String>,
            requestCode: Int
        ) {
            activity.startActivityForResult(
                activity.getSmartPermissionIntent(permissions),
                requestCode
            )
        }
    }

    /*权限列表*/
    private val mPermissions: MutableList<String> = mutableListOf()

    /**
     * 添加权限组
     */
    fun permission(vararg permission: String): PermissionsUtil {
        mPermissions.addAll(permission)
        return this
    }

    fun permission(vararg permissions: Array<String>): PermissionsUtil {
        val list = mutableListOf<String>()
        for (par in permissions) {
            list.addAll(par)
        }
        mPermissions.addAll(list)
        return this
    }

    fun request(callback: OnPermissionCallback) {
        val activity = context.findActivity()
        val debugMode = BuildConfig.DEBUG
        // 检查当前 Activity 状态是否是正常的，如果不是则不请求权限
        if (!checkActivityStatus(activity, debugMode)) return
        //必须要传入正常的权限或者权限组才能申请权限
        if (!checkPermissionArgument(mPermissions, debugMode)) return

        if (debugMode) {
            // 检查申请的存储权限是否符合规范
            context.checkStoragePermission(mPermissions)
            // 检查申请的定位权限是否符合规范
            checkLocationPermission(mPermissions)
            // 检查申请的权限和 targetSdk 版本是否能吻合
            checkTargetSdkVersion(context, mPermissions)
        }

        // 优化所申请的权限列表
        optimizeDeprecatedPermission(mPermissions)

        if (debugMode) {
            // 检测权限有没有在清单文件中注册
            context.checkPermissionManifest(mPermissions)
        }

        if (context.isGrantedPermissions(mPermissions)) {
            //证明这些权限已经全部授予通过，直接返回成功
            callback.onGranted(mPermissions, true)
            return
        }

        //申请没有授权过的权限
        getInterceptor().requestPermissions(activity!!, callback, mPermissions)
    }
}