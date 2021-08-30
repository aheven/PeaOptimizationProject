package com.mall.libcommon.social

/**
 * 社会化分享设置，在Application中调用
 */
object SocialConfig {
    //微信  配置信息
    private var wxAppID: String? = null//appId
    private var wxMchID: String? = null//商户号
    private var wxApiKey: String? = null//API密钥

    fun builder() = Builder()

    fun getWxAppId() = wxAppID

    class Builder {
        //*****************微信*******************
        fun wxAppID(wx_AppId: String): Builder {
            wxAppID = wx_AppId
            return this
        }

        fun wxMchID(wx_MchID: String): Builder {
            wxMchID = wx_MchID
            return this
        }

        fun wxApiKey(wx_ApiKey: String): Builder {
            wxApiKey = wx_ApiKey
            return this
        }

        fun build() {}
    }
}