package com.mall.login.adapter.binding

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.mall.libcommon.ext.toast
import com.mall.libcommon.utils.widget.text.TextClickableSpan
import com.mall.login.R

@BindingAdapter("userAgreementText")
fun setUserAgreementText(textView: TextView, text: String) {
    //已阅读并同意 《用户协议》《隐私协议》
    textView.movementMethod = LinkMovementMethod.getInstance()

    val spannableStringBuilder = SpannableStringBuilder(text)

    val red = ResourcesCompat.getColor(textView.resources, R.color.default_red, null)

    val clickableSpan = TextClickableSpan(red) {
        toast("clickable 1")
    }

    val clickableSpan1 = TextClickableSpan(red) {
        toast("clickable 2")
    }

    spannableStringBuilder.setSpan(clickableSpan, 7, 13, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    spannableStringBuilder.setSpan(
        clickableSpan1,
        13,
        text.length,
        Spanned.SPAN_INCLUSIVE_INCLUSIVE
    )
    textView.highlightColor = Color.TRANSPARENT
    textView.text = spannableStringBuilder
}