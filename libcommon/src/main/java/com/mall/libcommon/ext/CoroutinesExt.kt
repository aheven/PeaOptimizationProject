package com.mall.libcommon.ext

import com.mall.libcommon.model.BaseModel
import kotlinx.coroutines.coroutineScope

suspend inline fun <V> requestCoroutine(crossinline api: suspend () -> BaseModel<V>): V? =
    coroutineScope {
        try {
            val response: BaseModel<V> = api.invoke()
            if (!response.isSuccess) {
                dismissProgress()
                toast(response.msg)
            }
            response.data
        } catch (exception: Exception) {
            dismissProgress()
            null
        }
    }

suspend inline fun <V> requestCoroutineNotCatch(crossinline api: suspend () -> BaseModel<V>): V? =
    coroutineScope {
        val response: BaseModel<V> = api.invoke()
        if (!response.isSuccess) {
            dismissProgress()
            toast(response.msg)
            throw IllegalArgumentException("http response error,errorCode=${response.code},message=${response.msg}")
        }
        response.data
    }