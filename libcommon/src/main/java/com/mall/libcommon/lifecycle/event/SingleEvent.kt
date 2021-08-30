package com.mall.libcommon.lifecycle.event

/**
 * 对象包含了对数据的封装，每次发送一个SingleEvent事件，防止MutableLiveData在Fragment/Activity返回的时候，由于
 * ViewModel未重新初始化，导致对LiveData对象重新复制的问题。
 * 该方案旨在仅在未调用过并且只有重新发送了SingleEvent事件才调用
 */
class SingleEvent<out T>(
    private val content: T
) {
    var hasBeenHandled = false
        private set //允许外部读取，但不允许写入

    /**
     * 返回内容并阻止其再次使用
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * 返回内容，即使它已经被处理
     */
    fun peekContent(): T = content
}