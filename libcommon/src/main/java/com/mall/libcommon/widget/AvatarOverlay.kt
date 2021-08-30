package com.mall.libcommon.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import com.mall.libcommon.databinding.ViewAvatarOverlayBinding
import com.mall.libcommon.ext.px
import com.mall.libcommon.ext.setImageUrlRadius

class AvatarOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {
    private val viewAvatarOverlayBinding: ViewAvatarOverlayBinding

    init {
        orientation = HORIZONTAL
        viewAvatarOverlayBinding = ViewAvatarOverlayBinding.inflate(
            LayoutInflater.from(context), this, true
        )
    }

    fun setAvatars(avatars: List<String>) {
        for ((index,avatar) in avatars.withIndex()) {
            val imageView = ImageView(context)
            val layoutParams = LayoutParams(30.px(), 30.px())
            layoutParams.rightMargin = if (index == avatars.lastIndex) 0 else -(8.px())
            imageView.setImageUrlRadius(avatar, 30.px())
            viewAvatarOverlayBinding.avatarOverlayContainer.addView(imageView,0,layoutParams)
        }
    }
}