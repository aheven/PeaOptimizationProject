package com.mall.libcommon.ext

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

fun ImageView.setImageUrl(url: String) {
    Glide.with(this).load(url).fitCenter().into(this)
}

fun ImageView.setImageUrlRadius(url: String, radius: Int) {
    val requestOptions = RequestOptions.bitmapTransform(
        MultiTransformation(
            CenterCrop(),
            RoundedCorners(radius.dp())
        )
    )
    Glide.with(this).load(url).apply(requestOptions).into(this)
}