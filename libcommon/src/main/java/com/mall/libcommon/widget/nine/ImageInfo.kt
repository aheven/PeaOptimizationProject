package com.mall.libcommon.widget.nine

data class ImageInfo(
    val thumbnailUrl: String?,
    val bigImageUrl: String?,
    val imageViewHeight: Int,
    val imageViewWidth: Int,
    val imageViewX: Int,
    val imageViewY: Int
){
    constructor(thumbnailUrl: String,bigImageUrl: String):this(thumbnailUrl, bigImageUrl, 0, 0, 0, 0)
}
