package com.mall.libcommon.ext

import android.app.Activity
import android.app.Dialog
import com.mall.libcommon.utils.widget.dialog.LoadingDialog
import java.lang.ref.WeakReference

private var loadingDialog: Dialog? = null
private var weakReference: WeakReference<Activity>? = null

fun showProgress() {
    dismissProgress()

    if (weakReference?.get() == null || weakReference?.get()?.isFinishing == true)
        weakReference = WeakReference(getTopActivity())
    loadingDialog = weakReference?.get()?.let { LoadingDialog(it) }
    loadingDialog?.show()
}

fun dismissProgress() {
    if (loadingDialog?.isShowing == true && weakReference?.get()?.isFinishing == false) {
        loadingDialog?.dismiss()
    }
}