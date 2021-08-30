package com.mall.libcommon.ext

import android.util.Log
import com.mall.libcommon.BuildConfig

/**
 * 日志相关函数
 */
const val TAG = "HevenHolt"
val loggerEnable = BuildConfig.DEBUG

fun logi(msg: String) {
    Log.i(TAG, msg)
}

fun logw(msg: String){
    Log.w(TAG, msg)
}

fun loge(msg: String) {
    Log.e(TAG, msg)
}