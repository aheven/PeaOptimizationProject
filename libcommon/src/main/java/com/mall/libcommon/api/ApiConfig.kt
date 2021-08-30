package com.mall.libcommon.api

import com.mall.libcommon.BuildConfig


const val BASE_URL = "https://service.sqwvvip.com"
const val BASE_URL_TEST = "https://testservice.sqwvvip.com"

fun getBaseUrl(): String {
    return if (BuildConfig.DEBUG) BASE_URL_TEST else BASE_URL
}