package com.mall.libcommon.social.iml

import android.content.Context
import com.mall.libcommon.ext.logi
import com.mall.libcommon.social.SocialConfig
import com.mall.libcommon.social.api.SocialLogin
import com.mall.libcommon.social.api.SocialLoginListener
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WxLoginIml @Inject constructor(@ApplicationContext context: Context) : SocialLogin {


    private val mWxApi = createWxApi(context)

    companion object {
        private var mWxApi: IWXAPI? = null

        var loginListener: SocialLoginListener? = null

        fun createWxApi(context: Context): IWXAPI = mWxApi ?: synchronized(this) {
            WXAPIFactory.createWXAPI(context, SocialConfig.getWxAppId(), false).also {
                it.registerApp(SocialConfig.getWxAppId())
                mWxApi = it
            }
        }
    }

    override fun login() {
        if (loginListener == null) throw RuntimeException("must implement SocialLogin Listener")
        if (!mWxApi.isWXAppInstalled) {
            logi("未安装微信")
            loginListener?.unInstalled()
            return
        }
        val req = SendAuth.Req()
        req.scope = "snsapi_userinfo"
        req.state = "none"
        val startWXSuccess = mWxApi.sendReq(req)
        logi("启动微信：$startWXSuccess")
    }

    override fun addSocialLoginListener(listener: SocialLoginListener) {
        loginListener = listener
    }
}