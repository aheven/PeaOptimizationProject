package com.mall.libcommon.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView
import com.mall.libcommon.ext.px

class MaxWidthScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {
    private var mMaxWidth = 200.px()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        // 拿到原来宽度，高度的 mode 和 size
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)

        widthSize = getWidth(widthSize)

        val maxWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, widthMode)
        super.onMeasure(maxWidthMeasureSpec, heightMeasureSpec)
    }

    private fun getWidth(widthSize: Int): Int {
        return if (widthSize <= mMaxWidth) widthSize else mMaxWidth
    }
}