package com.mall.libcommon.ext

import java.io.File

fun File.createOrExistsFile(): Boolean {
    if (exists()) return isFile
    if (!parentFile.createOrExistsDir()) return false
    return createNewFile()
}

fun File?.createOrExistsDir() = this != null && if (this.exists()) isDirectory else mkdirs()