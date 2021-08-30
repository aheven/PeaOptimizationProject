package com.mall.libcommon.permission

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.SparseBooleanArray
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow

@TargetApi(Build.VERSION_CODES.M)
class PermissionFragment() : Fragment(), Runnable {
    companion object {
        /** 请求的权限组  */
        private const val REQUEST_PERMISSIONS = "request_permissions"

        /** 请求码（自动生成） */
        private const val REQUEST_CODE = "request_code"

        /** 权限请求码存放集合  */
        private val REQUEST_CODE_ARRAY = SparseBooleanArray()

        fun beginRequest(
            activity: Activity,
            permissions: List<String>,
            interceptor: IPermissionInterceptor?,
            callback: OnPermissionCallback
        ) {
            val fragment = PermissionFragment()
            val bundle = Bundle()
            var requestCode: Int
            // 请求码随机生成，避免随机产生之前的请求码，必须进行循环判断
            do {
                requestCode = getRandomRequestCode()
            } while (REQUEST_CODE_ARRAY[requestCode])
            // 标记这个请求码已经被占用
            REQUEST_CODE_ARRAY.put(requestCode, true)
            bundle.putInt(REQUEST_CODE, requestCode)
            bundle.putStringArrayList(REQUEST_PERMISSIONS, ArrayList(permissions))
            fragment.arguments = bundle
            // 设置保留实例，不会因为屏幕方向或配置变化而重新创建
            fragment.retainInstance = true
            // 设置权限回调监听
            fragment.setCallBack(callback)
            // 设置权限请求拦截器
            fragment.setInterceptor(interceptor)
            // 绑定到 Activity 上面
            fragment.attachActivity(activity)
        }

        private fun getRandomRequestCode(): Int {
            // 新版本的 Support 库限制请求码必须小于 65536
            // 旧版本的 Support 库限制请求码必须小于 256
            return Random().nextInt(2.0.pow(8.0).toInt())
        }
    }

    /** 是否申请了特殊权限  */
    private var mSpecialRequest = false

    /** 是否申请了危险权限  */
    private var mDangerousRequest = false

    /** 权限回调对象  */
    private var mCallBack: OnPermissionCallback? = null

    /** 权限请求拦截器 */
    private var mInterceptor: IPermissionInterceptor? = null

    /** Activity 屏幕方向  */
    private var mScreenOrientation = 0


    /**
     * 绑定 Activity
     */
    fun attachActivity(activity: Activity) {
        activity as AppCompatActivity
        activity.supportFragmentManager.beginTransaction().add(this, this.toString())
            .commitAllowingStateLoss()
    }

    /**
     * 解绑 Activity
     */
    fun detachActivity(activity: Activity) {
        activity as AppCompatActivity
        activity.supportFragmentManager.beginTransaction().remove(this).commitAllowingStateLoss()
    }

    /**
     * 设置权限监听回调监听
     */
    fun setCallBack(callback: OnPermissionCallback) {
        mCallBack = callback
    }

    /**
     * 设置权限请求拦截器
     */
    fun setInterceptor(interceptor: IPermissionInterceptor?) {
        this.mInterceptor = interceptor
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity ?: return
        // 如果当前没有锁定屏幕方向就获取当前屏幕方向并进行锁定
        mScreenOrientation = requireActivity().requestedOrientation
        if (mScreenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            return
        }
        val activityOrientation = requireActivity().resources.configuration.orientation
        try {
            // 兼容问题：在 Android 8.0 的手机上可以固定 Activity 的方向，但是这个 Activity 不能是透明的，否则就会抛出异常
            // 复现场景：只需要给 Activity 主题设置 <item name="android:windowIsTranslucent">true</item> 属性即可
            if (activityOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else if (activityOrientation == Configuration.ORIENTATION_PORTRAIT) {
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        } catch (e: IllegalStateException) {
            // java.lang.IllegalStateException: Only fullscreen activities can request orientation
            e.printStackTrace()
        }
    }

    override fun onDetach() {
        super.onDetach()
        activity ?: return
        if (mScreenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            return
        }
        // 为什么这里不用跟上面一样 try catch ？因为这里是把 Activity 方向取消固定，只有设置横屏或竖屏的时候才可能触发 crash
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onDestroy() {
        super.onDestroy()
        mCallBack = null
    }

    override fun onResume() {
        super.onResume()
        // 如果在 Activity 不可见的状态下添加 Fragment 并且去申请权限会导致授权对话框显示不出来
        // 所以必须要在 Fragment 的 onResume 来申请权限，这样就可以保证应用回到前台的时候才去申请权限
        if (mSpecialRequest) {
            return;
        }

        mSpecialRequest = true;
        requestSpecialPermission()
    }

    /**
     * 申请特殊权限
     */
    private fun requestSpecialPermission() {
        if (arguments == null || activity == null) {
            return
        }

        val permissions: List<String> =
            requireArguments().getStringArrayList(REQUEST_PERMISSIONS) ?: listOf()
        // 是否需要申请特殊权限
        var requestSpecialPermission = false
        // 判断当前是否包含特殊权限
        if (containsSpecialPermission(permissions)) {
            if (permissions.contains(Permission.MANAGE_EXTERNAL_STORAGE) && !requireActivity().isGrantedStoragePermission()) {
                //当前必须是 Android 11 及以上版本，因为 hasStoragePermission 在旧版本是拿权限做的判断，所以这里需要多判断一次版本
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    //跳转到存储权限设置界面
                    startActivityForResult(
                        requireActivity().getStoragePermissionIntent(), requireArguments().getInt(
                            REQUEST_CODE
                        )
                    )
                    requestSpecialPermission = true
                }
            }

            if (permissions.contains(Permission.REQUEST_INSTALL_PACKAGES) && !requireActivity().isGrantedInstallPermission()) {
                //跳转到安装权限设置页
                startActivityForResult(
                    requireActivity().getInstallPermissionIntent(), requireArguments().getInt(
                        REQUEST_CODE
                    )
                )
                requestSpecialPermission = true
            }

            if (permissions.contains(Permission.SYSTEM_ALERT_WINDOW) && !requireActivity().isGrantedWindowPermission()) {
                //跳转到悬浮窗设置页
                startActivityForResult(
                    requireActivity().getWindowPermissionIntent(), requireArguments().getInt(
                        REQUEST_CODE
                    )
                )
                requestSpecialPermission = true
            }

            if (permissions.contains(Permission.NOTIFICATION_SERVICE) && !requireActivity().isGrantedNotifyPermission()) {
                // 跳转到通知栏权限设置页面
                startActivityForResult(
                    requireActivity().getNotifyPermissionIntent(), requireArguments().getInt(
                        REQUEST_CODE
                    )
                )
                requestSpecialPermission = true
            }

            if (permissions.contains(Permission.WRITE_SETTINGS) && !requireActivity().isGrantedSettingPermission()) {
                // 跳转到系统设置权限设置页面
                startActivityForResult(
                    requireActivity().getSettingPermissionIntent(), requireArguments().getInt(
                        REQUEST_CODE
                    )
                )
                requestSpecialPermission = true
            }
        }

        // 当前必须没有跳转到悬浮窗或者安装权限界面
        if (!requestSpecialPermission) {
            requestDangerousPermission()
        }
    }

    /**
     * 申请危险权限
     */
    private fun requestDangerousPermission() {
        if (activity == null || arguments == null) return
        val requestCode = requireArguments().getInt(REQUEST_CODE)
        val allPermissions = requireArguments().getStringArrayList(REQUEST_PERMISSIONS)
        if (allPermissions.isNullOrEmpty()) return

        var locationPermission: MutableList<String>? = null
        // Android 10 定位策略发生改变，申请后台定位权限的前提是要有前台定位权限（授予了精确或者模糊任一权限）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && allPermissions.contains(Permission.ACCESS_BACKGROUND_LOCATION)) {
            locationPermission = mutableListOf()
            if (allPermissions.contains(Permission.ACCESS_COARSE_LOCATION)) {
                locationPermission.add(Permission.ACCESS_COARSE_LOCATION)
            }
            if (allPermissions.contains(Permission.ACCESS_FINE_LOCATION)) {
                locationPermission.add(Permission.ACCESS_FINE_LOCATION)
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || locationPermission.isNullOrEmpty()) {
            requestPermissions(allPermissions.toTypedArray(), requestCode)
            return
        }

        // 在 Android 10 的机型上，需要先申请前台定位权限，再申请后台定位权限
        beginRequest(requireActivity(), locationPermission, null, object : OnPermissionCallback {
            override fun onGranted(permissions: List<String>, all: Boolean) {
                if (!all || !isAdded) return
                // 前台定位权限授予了，现在申请后台定位权限
                beginRequest(
                    requireActivity(),
                    listOf(Permission.ACCESS_BACKGROUND_LOCATION),
                    null,
                    object : OnPermissionCallback {
                        override fun onGranted(permissions: List<String>, all: Boolean) {
                            if (!all || !isAdded) return
                            // 前台定位权限和后台定位权限都授予了

                            // 前台定位权限和后台定位权限都授予了
                            val grantResults = IntArray(allPermissions.size)
                            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED)
                            onRequestPermissionsResult(
                                requestCode,
                                allPermissions.toTypedArray(),
                                grantResults
                            )
                        }

                        override fun onDenied(permissions: List<String>, never: Boolean) {
                            if (!isAdded) return

                            // 后台定位授权失败，但是前台定位权限已经授予了
                            val grantResults = IntArray(allPermissions.size)
                            for (i in allPermissions.indices) {
                                grantResults[i] =
                                    if (Permission.ACCESS_BACKGROUND_LOCATION == allPermissions[i]) PackageManager.PERMISSION_DENIED else PackageManager.PERMISSION_GRANTED
                            }
                            onRequestPermissionsResult(
                                requestCode,
                                allPermissions.toTypedArray(),
                                grantResults
                            )
                        }
                    })
            }

            override fun onDenied(permissions: List<String>, never: Boolean) {
                if (!isAdded) return

                // 前台定位授权失败，并且无法申请后台定位权限
                val grantResults = IntArray(allPermissions.size)
                Arrays.fill(grantResults, PackageManager.PERMISSION_DENIED)
                onRequestPermissionsResult(requestCode, allPermissions.toTypedArray(), grantResults)
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (activity == null || arguments == null || mCallBack == null || requestCode != requireArguments().getInt(
                REQUEST_CODE
            )
        ) return

        val callback: OnPermissionCallback = mCallBack!!
        mCallBack = null

        val interceptor = mInterceptor
        mInterceptor = null

        for (i in permissions.indices) {
            val permission = permissions[i]

            if (isSpecialPermission(permission)) {
                // 如果这个权限是特殊权限，那么就重新进行权限检测
                grantResults[i] = requireActivity().getPermissionStatus(permission)
                continue
            }

            // 重新检查 Android 10.0 的三个新权限
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && (Permission.ACCESS_BACKGROUND_LOCATION == permission || Permission.ACTIVITY_RECOGNITION == permission
                        || Permission.ACCESS_MEDIA_LOCATION == permission)
            ) {
                // 如果当前版本不符合最低要求，那么就重新进行权限检测
                grantResults[i] = requireActivity().getPermissionStatus(permission)
                continue
            }

            // 重新检查 Android 9.0 的一个新权限
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P && Permission.ACCEPT_HANDOVER == permission) {
                // 如果当前版本不符合最低要求，那么就重新进行权限检测
                grantResults[i] = requireActivity().getPermissionStatus(permission)
                continue
            }

            // 重新检查 Android 8.0 的两个新权限
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && (Permission.ANSWER_PHONE_CALLS == permission || Permission.READ_PHONE_NUMBERS == permission)) {
                // 如果当前版本不符合最低要求，那么就重新进行权限检测
                grantResults[i] = requireActivity().getPermissionStatus(permission)
            }
        }

        // 释放对这个请求码的占用
        REQUEST_CODE_ARRAY.delete(requestCode)
        detachActivity(requireActivity())

        // 获取已授予的权限
        val grantedPermission = getGrantedPermissions(permissions, grantResults)

        // 如果请求成功的权限集合大小和请求的数组一样大时证明权限已经全部授予
        if (grantedPermission.size == permissions.size) {
            if (interceptor != null) {
                // 代表申请的所有的权限都授予了
                interceptor.grantedPermissions(
                    requireActivity(),
                    callback,
                    grantedPermission,
                    true
                )
            } else {
                callback.onGranted(grantedPermission, true)
            }
            return
        }

        // 获取被拒绝的权限
        val deniedPermission = getDeniedPermissions(permissions, grantResults)
        if (interceptor != null) {
            // 代表申请的权限中有不同意授予的，如果有某个权限被永久拒绝就返回 true 给开发人员，让开发者引导用户去设置界面开启权限
            interceptor.deniedPermissions(
                requireActivity(),
                callback,
                deniedPermission,
                requireActivity().isPermissionPermanentDenied(deniedPermission)
            )
        } else {
            callback.onDenied(
                deniedPermission,
                requireActivity().isPermissionPermanentDenied(deniedPermission)
            )
        }

        // 证明还有一部分权限被成功授予，回调成功接口
        if (grantedPermission.isNotEmpty()) {
            if (interceptor != null) {
                interceptor.grantedPermissions(
                    requireActivity(),
                    callback,
                    grantedPermission,
                    false
                )
            } else {
                callback.onGranted(grantedPermission, false)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (activity == null || arguments == null || requestCode != requireArguments().getInt(
                REQUEST_CODE
            ) || mDangerousRequest
        ) {
            return
        }
        mDangerousRequest = true
        // 需要延迟执行，不然有些华为机型授权了但是获取不到权限
        requireActivity().window.decorView.postDelayed(this, 300)
    }

    override fun run() {
        // 如果用户离开太久，会导致 Activity 被回收掉
        // 所以这里要判断当前 Fragment 是否有被添加到 Activity
        // 可在开发者模式中开启不保留活动来复现这个 Bug
        if (!isAdded) {
            return
        }
        // 请求其他危险权限
        requestDangerousPermission()
    }
}