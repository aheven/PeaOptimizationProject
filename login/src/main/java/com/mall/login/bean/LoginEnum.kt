package com.mall.login.bean

/**
 * 登录类型
 */
enum class LoginEnum(value: String) {
    PHONE_NUMBER("手机号"),
    WE_CHAT("微信"),
    PHONE_CODE("验证码"),
    PASSWORD("手机号密码登录"),
    INVITATION_CODE("邀请码注册"),
    MERGE_USER("合并用户")
}