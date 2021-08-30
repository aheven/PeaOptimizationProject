package com.mall.libcommon.utils.widget.dialog

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import com.mall.libcommon.R

class LoadingDialog(context: Context) : AppCompatDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setContentView(R.layout.dialog_loading)
    }
}