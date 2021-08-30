package com.mall.libcommon.model

open class BaseModel<T>(
        val code: Int = -1,
        val msg: String = "error"
) {
    val data: T? = null
    val isSuccess
        get() = code in 200..299
}