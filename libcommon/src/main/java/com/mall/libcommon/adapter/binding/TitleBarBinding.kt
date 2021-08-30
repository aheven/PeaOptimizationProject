package com.mall.libcommon.adapter.binding

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.navigation.findNavController
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.mall.libcommon.ext.getTopActivity

@BindingAdapter("titleBarListener")
fun setTitleBarListener(titleBar: TitleBar, titleBarListener: OnTitleBarListener) {
    titleBar.setOnTitleBarListener(titleBarListener)
}

@BindingAdapter("titleBarListener")
fun setTitleBarListener(titleBar: TitleBar, titleBarListener: String) {
    if (titleBarListener != "default") throw RuntimeException("setTitleBarListener only set default to back up")
    titleBar.setOnTitleBarListener(object : OnTitleBarListener {
        override fun onLeftClick(view: View?) {
            val isNavigateUp = view?.findNavController()?.navigateUp() ?: false
            if (!isNavigateUp)
                getTopActivity()?.finish()
        }

        override fun onTitleClick(view: View?) {
        }

        override fun onRightClick(view: View?) {
        }
    })
}