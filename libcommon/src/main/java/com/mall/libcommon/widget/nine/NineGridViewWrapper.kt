package com.mall.libcommon.widget.nine

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import com.mall.libcommon.R
import com.mall.libcommon.ext.sp


class NineGridViewWrapper @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private var moreNum = 0 //显示更多的数量
    private val maskColor: Int = 0x88000000.toInt() //默认的遮盖颜色
    private var textSize = 35.sp() //显示文字的大小单位sp
    private var textColor: Int = 0xFFFFFFFF.toInt() //显示文字的颜色

    private val textPaint: TextPaint//文字的画笔
    private var msg = "" //要绘制的文字

    private val porterDuffColorFilter = PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.NineGridViewWrapper)
        a.getDimensionPixelSize(R.styleable.NineGridViewWrapper_android_textSize, 35)
        a.recycle()

        textPaint = TextPaint().apply {
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            textSize = this@NineGridViewWrapper.textSize.toFloat()
            color = this@NineGridViewWrapper.textColor
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (moreNum > 0) {
            canvas.drawColor(maskColor)
            val baseY = height / 2 - (textPaint.ascent() + textPaint.descent()) / 2
            canvas.drawText(msg, (width / 2).toFloat(), baseY, textPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (drawable != null) {
                    /**
                     * 默认情况下，所有的从同一资源（R.drawable.XXX）加载来的drawable实例都共享一个共用的状态，
                     * 如果你更改一个实例的状态，其他所有的实例都会收到相同的通知。
                     * 使用使 mutate 可以让这个drawable变得状态不定。这个操作不能还原（变为不定后就不能变为原来的状态）。
                     * 一个状态不定的drawable可以保证它不与其他任何一个drawabe共享它的状态。
                     * 此处应该是要使用的 mutate()，但是在部分手机上会出现点击后变白的现象，所以没有使用
                     * 目前这种解决方案没有问题
                     */

//                drawable.mutate().colorFilter = porterDuffColorFilter
                    drawable.colorFilter = porterDuffColorFilter
                    ViewCompat.postInvalidateOnAnimation(this)
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (drawable != null) {
                    drawable.clearColorFilter()
                    ViewCompat.postInvalidateOnAnimation(this)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        setImageDrawable(null)
    }

    fun setMoreNum(moreNum: Int) {
        this.moreNum = moreNum
        msg = "+$moreNum"
        invalidate()
    }
}