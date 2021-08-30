package com.mall.login.bean

fun LoginParams.toCodeLoginParams(): LoginParams {
    code = authCode
    return this
}

class LoginParams {
    var authType: LoginEnum = LoginEnum.PHONE_CODE

    //手机号,条件选必填,手机号密码登录为必填
    var phoneNumber: String = ""

    //授权code,条件选必填,手机号码登录/ 手机验证码登录/淘宝/微信、微信小程序/苹果/手机号密码登录为必填
    var authCode: String = ""

    var code: String = ""

    var invitationCode:String? = null

    var inviteType: Int = 0//0-邀请好友，财富榜 1-限时免单

    var registerType: Int = 0//注册码

    var deviceNo: String? = null//友盟的umid

    var defaultAllocation = true//默认分配标记

    val isApp: Boolean = true

    var loginTrackId: Int? = null//注册轨迹id：邀请码注册为必填,合并用户时为必填
}