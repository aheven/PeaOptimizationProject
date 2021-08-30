package com.mall.login.arouter

import android.content.Context
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Interceptor
import com.alibaba.android.arouter.facade.callback.InterceptorCallback
import com.alibaba.android.arouter.facade.template.IInterceptor
import com.alibaba.android.arouter.launcher.ARouter
import com.mall.libcommon.ext.loge
import com.mall.login.api.LoginService
import com.mall.user.api.UserService

@Interceptor(priority = 1, name = "登录用拦截器")
class LoginInterceptor : IInterceptor {
    @Autowired
    lateinit var loginService: LoginService

    @Autowired
    lateinit var userService: UserService

    override fun init(context: Context?) {
        // 拦截器的初始化，会在sdk初始化的时候调用该方法，仅会调用一次
        loge("登录拦截器初始化")
        ARouter.getInstance().inject(this)
    }

    override fun process(postcard: Postcard, callback: InterceptorCallback) {
        if (userService.isLogin()) {
            callback.onContinue(postcard)
        } else {
            callback.onInterrupt(RuntimeException("用户未登录，跳转到登陆页"))
        }
    }
}