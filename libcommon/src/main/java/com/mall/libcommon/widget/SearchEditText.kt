package com.mall.libcommon.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods

@BindingMethods(
    BindingMethod(
        type = TextView::class,
        attribute = "android:onEditorAction",
        method = "setOnEditorActionListener"
    )
)
class SearchEditText(context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs) {
    companion object {
        const val LIMIT = 100L
    }

    private var mListener: OnTextChangeListener? = null
    private var mStartText: String = ""
    private val mAction = Runnable {
        if (mListener == null) return@Runnable
        if (mStartText != text.toString()) {
            mStartText = text.toString()
            mListener?.onTextChange(mStartText)
        }
    }

    /**
     * 在LIMIT时间内连续输入不触发文本
     */
    fun setOnTextChangeListener(listener: OnTextChangeListener?) {
        if (listener == null) return
        mListener = listener
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        removeCallbacks(mAction)
        postDelayed(mAction, LIMIT)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(mAction)
    }

    interface OnTextChangeListener {
        fun onTextChange(text: String)
    }

}