package com.mall.libcommon.di

import com.mall.libcommon.social.annotation.LoginQQ
import com.mall.libcommon.social.annotation.LoginWechat
import com.mall.libcommon.social.api.*
import com.mall.libcommon.social.iml.QQLoginIml
import com.mall.libcommon.social.iml.WxLoginIml
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class LoginModule {
    @Binds
    @LoginWechat
    abstract fun bindLoginWechat(wxLoginIml: WxLoginIml): SocialLogin

    @Binds
    @LoginQQ
    abstract fun bindLoginQQ(qqLoginIml: QQLoginIml): SocialLogin
}