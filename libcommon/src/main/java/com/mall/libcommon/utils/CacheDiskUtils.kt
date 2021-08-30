package com.mall.libcommon.utils

import android.os.Parcelable
import com.mall.libcommon.base.application
import com.mall.libcommon.ext.*
import kotlinx.coroutines.*
import java.io.File
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.collections.HashMap

/**
 * 本地内存（sdcard）缓存
 */
object CacheDiskUtils {
    private const val DEFAULT_MAX_SIZE = Long.MAX_VALUE
    private const val DEFAULT_MAX_COUNT = Int.MAX_VALUE
    private const val TIME_INFO_LEN = 14

    //type
    private const val CACHE_PREFIX = "cdu_"

    const val TYPE_BYTE = "by_"
    const val TYPE_STRING = "st_"
    const val TYPE_PARCELABLE = "pa_"

    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    private val CACHE_MAP = mutableMapOf<String, CacheDisk>()

    private lateinit var mDiskCacheManager: DiskCacheManager

    private lateinit var initJob: Job

    fun getInstance(): CacheDisk {
        return getInstance("", DEFAULT_MAX_SIZE, DEFAULT_MAX_COUNT)
    }

    fun getInstance(cacheName: String): CacheDisk {
        return getInstance(cacheName, DEFAULT_MAX_SIZE, DEFAULT_MAX_COUNT)
    }

    fun getInstance(maxSize: Long, maxCount: Int): CacheDisk {
        return getInstance("", maxSize, maxCount)
    }

    fun getInstance(cacheName: String, maxSize: Long, maxCount: Int): CacheDisk {
        val file =
            File(application.filesDir, if (cacheName.isEmpty()) "CacheDisk" else cacheName)
        return getInstance(file, maxSize, maxCount)
    }

    fun getInstance(cacheDir: File, maxSize: Long, maxCount: Int): CacheDisk {
        val cacheKey = "${cacheDir.absoluteFile}_$`maxSize`_$maxCount"
        return CACHE_MAP[cacheKey] ?: synchronized(this) {
            var cache = CACHE_MAP[cacheKey]
            if (cache == null) {
                cache = CacheDisk()
                mDiskCacheManager = getDiskCacheManager(cacheDir, maxSize, maxCount)
                initJob = scope.async {
                    mDiskCacheManager.initJobOK()
                }
            }
            cache.also { CACHE_MAP[cacheKey] = it }
        }
    }

    private fun getDiskCacheManager(
        cacheDir: File,
        maxSize: Long,
        maxCount: Int
    ): DiskCacheManager {
        return if (cacheDir.exists()) {
            DiskCacheManager(cacheDir, maxSize, maxCount)
        } else {
            if (cacheDir.mkdirs()) {
                DiskCacheManager(cacheDir, maxSize, maxCount)
            } else {
                throw RuntimeException("can't make dirs in ${cacheDir.absolutePath}")
            }
        }
    }

    class CacheDisk {
        /**
         * 保存String到本地内存
         */
        fun put(key: String, value: String) {
            put(key, value, -1)
        }

        fun put(key: String, value: String, saveTime: Int) {
            realPutBytes(TYPE_STRING + key, value.toByteArray(), saveTime)
        }

        /**
         * 从本地内存中获取String
         */
        fun getString(key: String): String? = getString(key, null)

        fun getString(key: String, defaultValue: String?): String? {
            val bytes = realGetBytes(TYPE_STRING + key) ?: return defaultValue
            return bytes.toString(Charset.defaultCharset())
        }

        /**
         * 保存Parcel对象到本地内存
         */
        fun put(key: String, parcelable: Parcelable) {
            put(key, parcelable, -1)
        }

        fun put(key: String, parcelable: Parcelable, saveTime: Int) {
            realPutBytes(TYPE_PARCELABLE + key, parcelable.toBytes(), saveTime)
        }

        /**
         * 从本地内存中获取Parcel对象
         */
        fun <T> getParcelable(key: String, creator: Parcelable.Creator<T>): T? {
            return getParcelable(key, creator, null)
        }

        fun <T> getParcelable(key: String, creator: Parcelable.Creator<T>, defaultValue: T?): T? {
            val bytes = realGetBytes(TYPE_PARCELABLE + key) ?: return defaultValue
            return bytes.toParcelable(creator)
        }

        /**
         * 保存byte[]到本地内存
         */
        fun put(key: String, value: ByteArray) {
            put(key, value, -1)
        }

        fun put(key: String, value: ByteArray, saveTime: Int) {
            realPutBytes(TYPE_BYTE + key, value, saveTime)
        }

        /**
         * 从本地内存中获取byte[]
         */
        fun getBytes(key: String): ByteArray? = getBytes(key, null)

        fun getBytes(key: String, defaultValue: ByteArray?): ByteArray? {
            return realGetBytes(TYPE_BYTE + key, defaultValue)
        }

        private fun realPutBytes(key: String, value: ByteArray, saveTime: Int) {
            scope.launch {
                val newValue = if (saveTime > 0) newByteArrayWithTime(saveTime, value) else value
                val file = mDiskCacheManager.getFileBeforePut(key)
                val success =
                    file.writeFileFromBytesByChannel(newValue, isForce = true)
                if (!success) return@launch
                mDiskCacheManager.updateModify(file)
                mDiskCacheManager.put(file)
            }
        }

        private fun realGetBytes(key: String): ByteArray? {
            return realGetBytes(key, null)
        }

        private fun realGetBytes(key: String, defaultValue: ByteArray?): ByteArray? {
            val file = mDiskCacheManager.getFileIfExists(key) ?: return defaultValue
            val data = file.readFile2BytesByChannel() ?: return defaultValue
            if (isDue(data)) {
                mDiskCacheManager.removeByKey(key)
                return defaultValue
            }
            mDiskCacheManager.updateModify(file)
            return getDataWithoutDueTime(data)
        }

        private fun newByteArrayWithTime(seconds: Int, data: ByteArray): ByteArray {
            val time = createDueTime(seconds).toByteArray()
            val content = ByteArray(time.size + data.size)
            System.arraycopy(time, 0, content, 0, time.size)
            System.arraycopy(data, 0, content, time.size, data.size)
            return content
        }

        private fun createDueTime(seconds: Int): String {
            return "_\$%010d\$_".format(
                Locale.getDefault(),
                System.currentTimeMillis() / 1000 + seconds
            )
        }

        private fun isDue(data: ByteArray): Boolean {
            val millis = getDueTime(data)
            return millis != -1L && System.currentTimeMillis() > millis
        }

        private fun getDueTime(data: ByteArray): Long {
            if (hasTimeInfo(data)) {
                val mills = String(copyOfRange(data, 2, 12))
                return try {
                    mills.toLong() * 1000
                } catch (e: NumberFormatException) {
                    -1
                }
            }
            return -1
        }

        private fun getDataWithoutDueTime(data: ByteArray?): ByteArray? {
            if (hasTimeInfo(data)) {
                return copyOfRange(data!!, TIME_INFO_LEN, data.size)
            }
            return data
        }

        private fun copyOfRange(original: ByteArray, from: Int, to: Int): ByteArray {
            val newLength = to - from
            if (newLength < 0) throw IllegalArgumentException("$from > $to")
            val copy = ByteArray(newLength)
            System.arraycopy(
                original,
                from,
                copy,
                0,
                (original.size - from).coerceAtMost(newLength)
            )
            return copy
        }

        private fun hasTimeInfo(data: ByteArray?): Boolean {
            return data != null
                    && data.size >= TIME_INFO_LEN
                    && data[0] == '_'.code.toByte()
                    && data[1] == '$'.code.toByte()
                    && data[12] == '$'.code.toByte()
                    && data[13] == '_'.code.toByte()
        }
    }

    private class DiskCacheManager(
        private val cacheDir: File,
        private val sizeLimit: Long,
        private val countLimit: Int
    ) {
        private val cacheSize = AtomicLong()
        private val cacheCount = AtomicInteger()

        private val lastUsageDates = Collections.synchronizedMap(HashMap<File, Long>())

        suspend fun initJobOK() = coroutineScope {
            var size = 0L
            var count = 0
            val cachedFiles = cacheDir.listFiles { _, name ->
                name.startsWith(CACHE_PREFIX)
            }
            if (!cachedFiles.isNullOrEmpty()) {
                for (cachedFile in cachedFiles) {
                    size += cachedFile.length()
                    count += 1
                    lastUsageDates[cachedFile] = cachedFile.lastModified()
                }
                cacheSize.getAndAdd(size)
                cacheCount.getAndAdd(count)
                logi("disk cache init job:cacheSize:$cacheSize,cacheCount:$cacheCount")
            }
        }

        suspend fun getFileBeforePut(key: String): File {
            initJob.join()
            val file = File(cacheDir, getCacheNameByKey(key))
            if (file.exists()) {
                cacheCount.addAndGet(-1)
                cacheSize.addAndGet(-file.length())
            }
            return file
        }

        fun getFileIfExists(key: String): File? {
            val file = File(cacheDir, getCacheNameByKey(key))
            if (!file.exists()) return null
            return file
        }

        fun getCacheNameByKey(key: String) =
            CACHE_PREFIX + key.substring(0, 3) + key.substring(3).hashCode()

        fun updateModify(file: File) {
            val millis = System.currentTimeMillis()
            file.setLastModified(millis)
            lastUsageDates[file] = millis
        }

        fun put(file: File) {
            cacheCount.addAndGet(1)
            cacheSize.addAndGet(file.length())
            while (cacheCount.get() > countLimit || cacheSize.get() > sizeLimit) {
                cacheSize.addAndGet(-removeOldest())
                cacheCount.addAndGet(-1)
            }
            logi("disk cache put job:cacheSize:$cacheSize,cacheCount:$cacheCount")
        }

        private fun removeOldest(): Long {
            if (lastUsageDates.isNullOrEmpty()) return 0
            var oldestUsage = Long.MAX_VALUE
            var oldestFile: File? = null
            synchronized(lastUsageDates) {
                for ((file, lastValueUsage) in lastUsageDates) {
                    if (lastValueUsage < oldestUsage) {
                        oldestUsage = lastValueUsage
                        oldestFile = file
                    }
                }
            }
            if (oldestFile == null) return 0
            val fileSize = oldestFile!!.length()
            if (oldestFile!!.delete()) {
                lastUsageDates.remove(oldestFile)
                return fileSize
            }
            return 0
        }

        fun removeByKey(key: String): Boolean {
            val file = getFileIfExists(key) ?: return true
            val fileLength = file.length()
            if (!file.delete()) return false
            cacheSize.addAndGet(-fileLength)
            cacheCount.addAndGet(-1)
            lastUsageDates.remove(file)
            logi("disk cache remove job:cacheSize:$cacheSize,cacheCount:$cacheCount")
            return true
        }
    }

}