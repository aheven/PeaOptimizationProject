package com.mall.libcommon.utils.widget.text

import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.annotation.ColorInt

class TextClickableSpan(
    @ColorInt private val color: Int,
    private val listener: () -> Unit
) : ClickableSpan() {
    override fun onClick(widget: View) {
        listener.invoke()
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.color = color
        ds.isUnderlineText = false
    }
}