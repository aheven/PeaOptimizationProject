package com.mall.libcommon.ext

import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer

fun File.writeFileFromBytesByChannel(bytes: ByteArray, isForce: Boolean): Boolean {
    return writeFileFromBytesByChannel(bytes, false, isForce)
}

/**
 * 通过FileChannel写入数据
 * @param isForce 是否同时将文件元数据（权限信息等）写到磁盘上，即时写入。
 */
fun File.writeFileFromBytesByChannel(bytes: ByteArray, append: Boolean, isForce: Boolean): Boolean =
    try {
        FileOutputStream(this, append).channel.use { fc ->
            fc.position(fc.size())
            fc.write(ByteBuffer.wrap(bytes))
            if (isForce) fc.force(true)
            true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }


fun File.readFile2BytesByChannel(): ByteArray? =
    try {
        RandomAccessFile(this, "r").channel.use { fc ->
            val byteBuffer = ByteBuffer.allocate(fc.size().toInt())
            while (true) {
                if (!((fc.read(byteBuffer)) > 0)) break
            }
            byteBuffer.array()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }