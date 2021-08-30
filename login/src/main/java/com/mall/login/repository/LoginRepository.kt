package com.mall.login.repository

import com.mall.libcommon.http.model.HttpParam
import com.mall.login.api.ApiLoginService
import com.mall.login.bean.LoginParams
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val loginService: ApiLoginService
) {
    /**
     * 请求验证码
     */
    suspend fun requestCode(phone: String) = loginService.requestVerifyCode(phone)

    /**
     * 登录
     */
    suspend fun login(loginParams: LoginParams) = loginService.login(loginParams)

    /**
     * 注册轨迹绑定手机号
     */
    suspend fun bindingMobileForTrack(loginParams: LoginParams) =
        loginService.bindingMobileForTrack(loginParams)

    /**
     *
     */
    suspend fun queryUserByInvitationCode(params: HttpParam) =
        loginService.queryUserByInvitationCode(params)
}