package com.mall.user.provider.iml

import android.content.Context
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.callback.NavigationCallback
import com.alibaba.android.arouter.launcher.ARouter
import com.mall.libcommon.ext.loge
import com.mall.user.api.UserService
import com.mall.user.bean.LoginInfo
import com.mall.user.interrupt.LoginInterrupt

@Route(path = UserService.USER_LAUNCHER, name = "用户信息服务")
class UserServiceIml : UserService {

    override fun loginSuccess(loginInfo: LoginInfo) {
        loge(loginInfo.toString())
    }

    override fun isLogin(): Boolean {
        return false
    }

    override fun providerUserCenterFragment(context: Context): Fragment {
        return ARouter.getInstance().build("/user/center").navigation(context, LoginInterrupt()) as Fragment
    }

    override fun startUserCenterActivity(context: Context) {
        ARouter.getInstance().build("/user/center").navigation(context, LoginInterrupt())
    }

    override fun init(context: Context) {
    }
}