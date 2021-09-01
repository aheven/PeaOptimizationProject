package com.mall.libcommon.permission

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class PermissionContract : ActivityResultContract<String, Unit>() {
    override fun createIntent(context: Context, input: String): Intent {
        return when (input) {
            Permission.MANAGE_EXTERNAL_STORAGE -> context.getStoragePermissionIntent()
            Permission.REQUEST_INSTALL_PACKAGES -> context.getInstallPermissionIntent()
            Permission.SYSTEM_ALERT_WINDOW -> context.getWindowPermissionIntent()
            Permission.NOTIFICATION_SERVICE -> context.getNotifyPermissionIntent()
            Permission.WRITE_SETTINGS -> context.getSettingPermissionIntent()
            else -> context.getApplicationDetailsIntent()
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?) {

    }
}