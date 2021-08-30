package com.mall.libcommon.social.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mall.libcommon.ext.loge
import com.mall.libcommon.ext.logi
import com.mall.libcommon.social.api.SocialLoginListener
import com.mall.libcommon.social.iml.WxLoginIml
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler

open class SocialWXEntryActivity : AppCompatActivity(), IWXAPIEventHandler {
    private val wxApi: IWXAPI by lazy {
        WxLoginIml.createWxApi(application)
    }

    private val listener: SocialLoginListener? by lazy {
        WxLoginIml.loginListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wxApi.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        wxApi.handleIntent(intent, this)
    }


    override fun onReq(baseReq: BaseReq) {
        logi("微信登录|分享授权--onReq--openId--${baseReq.openId}")
    }

    override fun onResp(resp: BaseResp) {
        logi("微信登录授权--onResp：errCode:${resp.errCode},errStr:${resp.errStr}")
        if (ConstantsAPI.COMMAND_SENDAUTH == resp.type) {
            handleRespLogin(resp as SendAuth.Resp)
        }
        finish()
    }

    private fun handleRespLogin(resp: SendAuth.Resp) {
        when (resp.errCode) {
            BaseResp.ErrCode.ERR_OK -> {
                logi("登录成功：authCode:${resp.code}")
                listener?.loginSuccess(resp.code)
            }
            BaseResp.ErrCode.ERR_USER_CANCEL -> {
                loge("取消授权登录")
                listener?.loginCancel()
            }
            BaseResp.ErrCode.ERR_AUTH_DENIED -> {
                loge("授权失败")
                listener?.loginFailed()
            }
        }
    }
}