package com.mall.user.interrupt

import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavigationCallback
import com.alibaba.android.arouter.launcher.ARouter

class LoginInterrupt : NavigationCallback {
    override fun onFound(postcard: Postcard) {
    }

    override fun onLost(postcard: Postcard) {
    }

    override fun onArrival(postcard: Postcard) {
    }

    override fun onInterrupt(postcard: Postcard) {
        ARouter.getInstance().build("/login/home").greenChannel().navigation()
    }
}