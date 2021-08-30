package com.mall.libcommon.utils

import android.util.LruCache


object CacheMemoryUtils {
    private const val DEFAULT_MAX_COUNT = 1024 //1kb

    private val CACHE_MAP = mutableMapOf<String, CacheMemory>()

    fun getInstance(): CacheMemory {
        return getInstance(DEFAULT_MAX_COUNT)
    }

    fun getInstance(maxCount: Int): CacheMemory {
        return getInstance(maxCount.toString(), maxCount)
    }

    fun getInstance(cacheKey: String, maxCount: Int): CacheMemory {
        return CACHE_MAP[cacheKey] ?: synchronized(this) {
            var cache = CACHE_MAP[cacheKey]
            if (cache == null) {
                cache = CacheMemory(cacheKey, LruCache(maxCount))
            }
            cache.also { CACHE_MAP[cacheKey] = it }
        }
    }

    class CacheMemory(
        private val mCacheKey: String,
        private val mMemoryCache: LruCache<String, CacheValue>
    ) {
        override fun toString(): String {
            return mCacheKey + "@" + Integer.toHexString(hashCode())
        }

        fun put(key: String, value: Any) {
            put(key, value, -1)
        }

        fun put(key: String, value: Any, saveTime: Int) {
            val dueTime: Long =
                if (saveTime < 0) -1L else System.currentTimeMillis() + saveTime * 1000
            mMemoryCache.put(key, CacheValue(dueTime, value))
        }

        operator fun <T> get(key: String): T? {
            return get(key, null)
        }

        operator fun <T> get(key: String, defaultValue: T?): T? {
            val cacheValue = mMemoryCache[key] ?: return defaultValue
            if (cacheValue.dueTime == -1L || cacheValue.dueTime >= System.currentTimeMillis()) {
                return cacheValue.value as T
            }
            mMemoryCache.remove(key)
            return defaultValue
        }

        fun getCacheCount(): Int {
            return mMemoryCache.size()
        }

        fun remove(key: String): Any? {
            val remove = mMemoryCache.remove(key) ?: return null
            return remove.value
        }

        fun clear() {
            mMemoryCache.evictAll()
        }
    }

    /**
     * @param dueTime 超时时间，单位：s
     */
    data class CacheValue(val dueTime: Long, val value: Any)
}