package com.mall.login

import com.alibaba.android.arouter.facade.annotation.Route
import com.mall.libcommon.base.BaseActivity
import com.mall.libcommon.statusbar.StatusBarStyle
import com.mall.login.databinding.LoginActivityBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@Route(path = "/login/home")
class LoginActivity : BaseActivity<LoginActivityBinding>() {
    override fun initView() {
    }

    override fun initData() {
    }

    override fun setCustomStatusBarStyle(): StatusBarStyle = StatusBarStyle.TRANSLATE
}