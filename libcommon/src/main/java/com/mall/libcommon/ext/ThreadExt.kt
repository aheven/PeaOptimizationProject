package com.mall.libcommon.ext

import android.os.Looper

fun isMainThread() = Looper.myLooper() == Looper.getMainLooper()