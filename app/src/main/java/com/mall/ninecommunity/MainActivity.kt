package com.mall.ninecommunity

import android.view.View
import com.mall.libcommon.base.BaseActivity
import com.mall.libcommon.ext.loge
import com.mall.libcommon.permission.OnPermissionCallback
import com.mall.libcommon.permission.Permission
import com.mall.libcommon.permission.PermissionsUtil
import com.mall.ninecommunity.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun initView() {

    }

    fun clickButton(view: View) {
        PermissionsUtil.with(this)
            .permission(Permission.CAMERA, Permission.READ_CALENDAR)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: List<String>, all: Boolean) {
                    loge("permission onGranted:{$permissions},all=$all")
                }

                override fun onDenied(permissions: List<String>, never: Boolean) {
                    loge("permission onDenied:{$permissions},all=$never")
                }
            })
    }

    override fun initData() {
    }
}