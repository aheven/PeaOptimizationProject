package com.mall.ninecommunity

import com.mall.libcommon.base.BaseApplication
import com.mall.libcommon.interceptor.PermissionInterceptor
import com.mall.libcommon.permission.PermissionsUtil
import com.mall.libcommon.social.SocialConfig
import com.umeng.commonsdk.UMConfigure
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MyApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        UMConfigure.preInit(this, null, null)
        UMConfigure.setLogEnabled(BuildConfig.DEBUG)

        //TODO 延迟化调用友盟初始化
        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, null)

        SocialConfig.builder().wxAppID("wx04ce88dd34b158bd").build()
        PermissionsUtil.setInterceptor(PermissionInterceptor())
    }
}