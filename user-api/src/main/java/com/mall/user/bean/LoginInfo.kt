package com.mall.user.bean

/**
 * 登录成功获取到的令牌
 */
data class LoginInfo(
    val tokenType: String?,//令牌类型
    val accessToken: String?,//令牌
    val refreshToken: String?,//刷新令牌
    val loginTrackId: Int?//注册轨迹id：邀请码注册为必填,合并用户时为必填
)