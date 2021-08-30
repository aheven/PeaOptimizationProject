package com.mall.libcommon.http.interceptor

import android.os.Build
import com.mall.libcommon.base.application
import com.mall.libcommon.ext.getAppMetaData
import com.mall.libcommon.ext.getAppVersionCode
import com.mall.libcommon.ext.getAppVersionName
import okhttp3.Interceptor
import okhttp3.Response

class OkHttpHeadPublicParamsInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        builder.addHeader("pk", application.packageName)
            .addHeader("channel", application.getAppMetaData("UMENG_CHANNEL") ?: "official")
            .addHeader("osVersion", Build.VERSION.RELEASE)
            .addHeader("versionCode", application.getAppVersionCode().toString())
            .addHeader("versionName", application.getAppVersionName())
            .addHeader("clientType", "0")
        return chain.proceed(builder.build())
    }
}