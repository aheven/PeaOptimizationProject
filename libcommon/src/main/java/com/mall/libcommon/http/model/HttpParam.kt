package com.mall.libcommon.http.model

/**
 *Time:2020/1/14
 *Author:HevenHolt
 *Description:使用类型包装Map
 */
class HttpParam private constructor() : HashMap<String, Any>() {
    companion object {
        fun obtain(): HttpParam = HttpParam()

        fun obtain(key: String, value: Any): HttpParam {
            val httpParam = HttpParam()
            httpParam[key] = value
            return httpParam
        }
    }
}