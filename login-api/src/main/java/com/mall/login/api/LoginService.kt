package com.mall.login.api

import android.content.Context
import com.alibaba.android.arouter.facade.template.IProvider

interface LoginService : IProvider {
    companion object {
        const val LOGIN_LAUNCHER = "/login/service"
    }
}