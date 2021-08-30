package com.mall.libcommon.widget.nine

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.mall.libcommon.R

/**
 * 以九宫格得形式展示图片
 */
class NineGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    companion object {
        const val MODE_FILL = 0 //填充模式，类似于微信

        const val MODE_GRID = 1 //网格模式，类似于QQ，4张图会 2X2布局

        const val MODE_TILE = 2  //平铺网格模式

        private var imageLoader: ImageLoader? = null

        fun setImageLoader(imageLoader: ImageLoader) {
            Companion.imageLoader = imageLoader
        }
    }

    private var tileMaxHeight = 200 // 平铺模式最大高度
    private var singleImageSize = 500 // 单张图片时的最大大小,单位dp
    private var singleImageRatio = 1.0f // 单张图片的宽高比(宽/高)
    private var maxImageSize = 9 // 最大显示的图片数
    private var gridSpacing = 3 // 宫格间距，单位dp
    private var mode = MODE_FILL // 默认使用fill模式

    private var columnCount = 0// 列数
    private var rowCount = 0// 行数
    private var gridWidth = 0// 宫格宽度
    private var gridHeight = 0// 宫格高度

    private var mImageInfo: MutableList<ImageInfo>? = null
    private val imageViews: MutableList<ImageView> = mutableListOf()
    private lateinit var mAdapter: NineGridViewAdapter

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.NineGridView)
        gridSpacing = a.getDimensionPixelSize(R.styleable.NineGridView_grid_spacing, 3)
        singleImageSize = a.getDimensionPixelSize(R.styleable.NineGridView_single_image_size, 300)
        tileMaxHeight = a.getDimensionPixelSize(R.styleable.NineGridView_tile_max_height, 300)
        singleImageRatio = a.getFloat(R.styleable.NineGridView_single_image_ratio, 1.0f)
        maxImageSize = a.getInt(R.styleable.NineGridView_max_image_size, 9)
        mode = a.getInt(R.styleable.NineGridView_mode, 0)
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = 0
        val totalWidth = width - paddingLeft - paddingRight
        if (!mImageInfo.isNullOrEmpty()) {
            if (mode == MODE_TILE) { //平铺模式
                if (mImageInfo!!.size == 1) {
                    gridWidth = totalWidth
                    gridHeight = tileMaxHeight
                } else if (mImageInfo!!.size == 2 || mImageInfo!!.size == 4) {
                    gridWidth = (totalWidth - gridSpacing) / 2
                    gridHeight = (tileMaxHeight * 0.7).toInt() //自定义高度（单张高度的0.7倍）
                } else {
                    gridWidth = (totalWidth - gridSpacing * 2) / 3
                    gridHeight = (tileMaxHeight * 0.5).toInt() //自定义高度
//                    gridHeight = gridWidth ; //宽高相等
                }
            } else {// 其它样式（朋友圈 、 QQ空间）
                if (mImageInfo!!.size == 1) {
                    gridWidth = if (singleImageSize > totalWidth) totalWidth else singleImageSize
                    gridHeight = (gridWidth / singleImageRatio).toInt()
                    //矫正图片显示区域大小，不允许超过最大显示范围
                    if (gridHeight > singleImageSize) {
                        val ratio = singleImageSize * 1.0f / gridHeight
                        gridWidth = (gridWidth * ratio).toInt()
                        gridHeight = singleImageSize
                    }
                } else {
                    //这里无论是几张图片，宽高都按总宽度的 1/3
                    gridWidth = (totalWidth - gridSpacing * 2) / 3
                    gridHeight = gridWidth
                }
            }
            width =
                gridWidth * columnCount + gridSpacing * (columnCount - 1) + paddingLeft + paddingRight
            height =
                gridHeight * rowCount + gridSpacing * (rowCount - 1) + paddingTop + paddingBottom
        }
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (mImageInfo.isNullOrEmpty()) return
        val childrenCount = mImageInfo!!.size
        for (i in 0 until childrenCount) {
            val childrenView = getChildAt(i) as ImageView

            val rowNum = i / columnCount
            val columnNum = i % columnCount
            val left = (gridWidth + gridSpacing) * columnNum + paddingLeft
            val top = (gridHeight + gridSpacing) * rowNum + paddingTop
            val right = left + gridWidth
            val bottom = top + gridHeight
            childrenView.layout(left, top, right, bottom)

            if (!mImageInfo.isNullOrEmpty()) {
                val thumbnailUrl = mImageInfo!![i].thumbnailUrl
                val bigImageUrl = mImageInfo!![i].bigImageUrl
                if (thumbnailUrl.isNullOrEmpty() && bigImageUrl.isNullOrEmpty()) return
                imageLoader?.onDisplayImage(
                    context,
                    childrenView,
                    if (thumbnailUrl.isNullOrEmpty()) bigImageUrl!! else thumbnailUrl
                )
            }
        }
    }

    fun setAdapter(adapter: NineGridViewAdapter) {
        mAdapter = adapter

        var imageInfo = adapter.imageInfo
        if (imageInfo.isNullOrEmpty()) {
            visibility = GONE
            return
        } else {
            visibility = VISIBLE
        }

        var imageCount = imageInfo.size
        if (maxImageSize in 1 until imageCount) {
            imageInfo = imageInfo.subList(0, maxImageSize)
            imageCount = imageInfo.size
        }

        //默认是3列显示，行数根据图片的数量决定
        rowCount = imageCount / 3 + (if (imageCount % 3 == 0) 0 else 1)
        columnCount = 3
        //grid模式下，显示4张使用2X2模式
        if (mode == MODE_GRID) {
            if (imageCount == 4) {
                rowCount = 2
                columnCount = 2
            }
        } else if (mode == MODE_TILE) {
            if (imageCount == 2) {
                rowCount = 1
                columnCount = 2
            } else if (rowCount == 2) {
                rowCount = 2
                columnCount = 2
            }
        }

        //保证View的复用，避免重复创建
        if (mImageInfo.isNullOrEmpty()) {
            for (i in 0 until imageCount) {
                val iv: ImageView = getImageView(i)
                addView(iv, generateDefaultLayoutParams())
            }
        } else {
            val oldViewCount = mImageInfo!!.size
            val newViewCount = imageCount
            if (oldViewCount > newViewCount) {
                removeViews(newViewCount, oldViewCount - newViewCount)
            } else if (oldViewCount < newViewCount) {
                for (i in oldViewCount until newViewCount) {
                    val iv: ImageView = getImageView(i)
                    addView(iv, generateDefaultLayoutParams())
                }
            }
        }

        //修改最后一个条目，决定是否显示更多
        if (adapter.imageInfo.size > maxImageSize) {
            val child = getChildAt(maxImageSize - 1)
            if (child is NineGridViewWrapper) {
                child.setMoreNum(adapter.imageInfo.size - maxImageSize)
            }
        }

        mImageInfo = imageInfo.toMutableList()
        requestLayout()
    }

    private fun getImageView(index: Int): ImageView {
        return if (index < imageViews.size) {
            imageViews[index]
        } else {
            mAdapter.generateImageView(context).apply {
                setOnClickListener {
                    mAdapter.onImageItemClick(context, this@NineGridView, index, mAdapter.imageInfo)
                }
                imageViews.add(this)
            }
        }
    }

    /**
     * 需要实现该接口，以确定如何加载和显示图片，如果不设置，则图片不加载
     */
    interface ImageLoader {
        fun onDisplayImage(context: Context, imageView: ImageView, url: String)

        fun onDisplayImage(context: Context, imageView: ImageView, @DrawableRes src: Int)
    }
}