package com.mall.user.api

import android.content.Context
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.template.IProvider
import com.mall.user.bean.LoginInfo

/**
 * 用户模块接口提供
 */
interface UserService : IProvider {
    companion object {
        const val USER_LAUNCHER = "/user/service"
    }

    /**
     * @param loginInfo 登录成功后获取的令牌
     */
    fun loginSuccess(loginInfo: LoginInfo)

    /**
     * 是否登录
     */
    fun isLogin(): Boolean

    /**
     * 提供用户登录页
     */
    fun providerUserCenterFragment(context: Context): Fragment

    /**
     * 跳转用户页
     */
    fun startUserCenterActivity(context: Context)
}