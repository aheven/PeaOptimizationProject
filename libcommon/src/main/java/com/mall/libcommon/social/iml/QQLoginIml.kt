package com.mall.libcommon.social.iml

import com.mall.libcommon.ext.loge
import com.mall.libcommon.social.api.SocialLogin
import com.mall.libcommon.social.api.SocialLoginListener
import javax.inject.Inject

class QQLoginIml @Inject constructor() : SocialLogin {
    private var listener: SocialLoginListener? = null

    override fun login() {
        loge("222222222222222")
    }

    override fun addSocialLoginListener(listener: SocialLoginListener) {
        this.listener = listener
    }
}