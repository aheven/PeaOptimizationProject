package com.mall.login.listener

/**
 * 获取验证码是否可点状态
 */
interface LoginCodeEnableListener {
    /**
     * @param enable true:可点；false:不可点
     */
    fun loginCodeEnable(enable: Boolean)
}