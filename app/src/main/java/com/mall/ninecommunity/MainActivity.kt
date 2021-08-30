package com.mall.ninecommunity

import android.view.View
import com.mall.libcommon.base.BaseActivity
import com.mall.libcommon.ext.toast
import com.mall.libcommon.permission.OnPermissionCallback
import com.mall.libcommon.permission.Permission
import com.mall.libcommon.permission.PermissionsUtil
import com.mall.ninecommunity.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun initView() {

    }

    fun clickButton(view: View) {
        PermissionsUtil.with(this)
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: List<String>, all: Boolean) {
                    if (all) {
                        toast("获取存储权限成功！")
                    }
                }

                override fun onDenied(permissions: List<String>, never: Boolean) {
                    if (never) {
                        toast("被永久禁用权限：$permissions")
                    }
                }
            })
    }

    override fun initData() {
    }
}