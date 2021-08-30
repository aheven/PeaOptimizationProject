package com.mall.libcommon.ext

/**
 * 校验String参数，不通过弹出Toast。
 *
 * @return true:uncheck;false:checked
 */
fun String?.check(message: String): Boolean {
    val nullOrEmpty = isNullOrEmpty()
    if (nullOrEmpty) {
        toast(message)
    }
    return nullOrEmpty
}

fun Boolean?.check(message: String): Boolean {
    val nullOrEmpty = this != true
    if (nullOrEmpty) {
        toast(message)
    }
    return nullOrEmpty
}

fun Int?.check(message: String): Boolean {
    val nullOrEmpty = this == null || this == 0
    if (nullOrEmpty) {
        toast(message)
    }
    return nullOrEmpty
}