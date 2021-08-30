package com.mall.login.provider.iml

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.mall.login.api.LoginService

@Route(path = LoginService.LOGIN_LAUNCHER, name = "登录服务")
class LoginServiceIml : LoginService {
    override fun init(context: Context) {

    }
}