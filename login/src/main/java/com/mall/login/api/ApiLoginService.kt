package com.mall.login.api

import com.mall.libcommon.http.model.HttpParam
import com.mall.libcommon.model.BaseModel
import com.mall.login.bean.LoginParams
import com.mall.login.bean.LoginUserInfo
import com.mall.user.bean.LoginInfo
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiLoginService {
    /**
     * 请求验证码
     */
    @POST("user-center/platformuser/tplatformuser/sendCodeForMobile")
    suspend fun requestVerifyCode(@Query("phoneNumber") phone: String): BaseModel<Boolean>

    /**
     * 登录接口
     */
    @POST("auth-center/auth/login")
    suspend fun login(@Body loginParams: LoginParams): BaseModel<LoginInfo>

    /**
     * 注册轨迹绑定手机号
     */
    @POST("auth-center/auth/bindingMobileForTrack")
    suspend fun bindingMobileForTrack(@Body loginParams: LoginParams): BaseModel<String>

    /**
     * 通过邀请码查询用户信息
     */
    @POST("user-center/platformuser/tplatformuser/queryUserByCode")
    suspend fun queryUserByInvitationCode(params:HttpParam):BaseModel<LoginUserInfo>
}