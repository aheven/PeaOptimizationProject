package com.mall.libcommon.social.api

interface SocialLogin {
    fun login()

    fun addSocialLoginListener(listener: SocialLoginListener)
}

interface SocialLoginListener {
    /**
     * 未安装应用处理
     */
    fun unInstalled() {}

    /**
     * 登录成功
     *
     * @param code 授权码
     */
    fun loginSuccess(code: String)

    /**
     * 登录取消
     */
    fun loginCancel() {}

    /**
     * 登录失败
     */
    fun loginFailed() {}
}