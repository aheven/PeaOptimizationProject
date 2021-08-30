package com.mall.login.viewmodel

import android.view.View
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.launcher.ARouter
import com.mall.libcommon.ext.*
import com.mall.libcommon.http.model.HttpParam
import com.mall.libcommon.lifecycle.event.SingleEvent
import com.mall.libcommon.social.annotation.LoginWechat
import com.mall.libcommon.social.api.SocialLogin
import com.mall.libcommon.social.api.SocialLoginListener
import com.mall.login.R
import com.mall.login.bean.LoginEnum
import com.mall.login.bean.LoginParams
import com.mall.login.bean.LoginUserInfo
import com.mall.login.bean.toCodeLoginParams
import com.mall.login.listener.LoginCodeEnableListener
import com.mall.login.repository.LoginRepository
import com.mall.user.api.UserService
import com.mall.user.bean.LoginInfo
import com.umeng.commonsdk.UMConfigure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    @LoginWechat private val socialLogin: SocialLogin
) : ViewModel(), SocialLoginListener {
    val loginParams: LoginParams = LoginParams()

    val codeString: MutableLiveData<String> by lazy {
        MutableLiveData(getResourceString(R.string.login_phone_code))
    }

    //注册轨迹Id,当有值时，跳转到手机注册轨迹绑定页面
    //注意由于ViewModel依赖于Activity，Fragment返回时并未重新初始化
    //导致无法返回，使用SingleEvent封装数据，保证数据已经处理过后，不再处理数据
    val loginTrackId: MutableLiveData<SingleEvent<Int>> by lazy {
        MutableLiveData()
    }

    //用户协议勾选项
    val agreementChecked: MutableLiveData<Boolean> = MutableLiveData()

    private var loginCodeEnableListener: LoginCodeEnableListener? = null

    fun setLoginCodeEnableListener(loginCodeEnableListener: LoginCodeEnableListener) {
        this.loginCodeEnableListener = loginCodeEnableListener
    }

    @Autowired
    lateinit var userService: UserService

    init {
        ARouter.getInstance().inject(this)
        socialLogin.addSocialLoginListener(this)
    }

    /**
     * 手机验证码登录
     */
    fun loginByPhoneCode() {
        val phone = loginParams.phoneNumber
        val code = loginParams.authCode

        if (phone.check("手机号码不能为空")) return
        if (code.check("验证码不能为空")) return
        if (agreementChecked.value.check("请阅读并勾选用户协议选项")) return

        loginParams.authType = LoginEnum.PHONE_CODE

        login()
    }

    /**
     * 微信登录
     */
    fun loginByWechat() {
        if (agreementChecked.value.check("请阅读并勾选用户协议选项")) return

        showProgress()
        socialLogin.login()
    }

    private fun loginByWechat(code: String) {
        loginParams.authCode = code
        loginParams.authType = LoginEnum.WE_CHAT

        login()

        loginParams.authCode = ""
    }

    /**
     * 登录
     */
    private fun login() {
        showProgress()
        viewModelScope.launch {
            val loginInfo: LoginInfo = requestCoroutine {
                loginRepository.login(loginParams)
            } ?: return@launch
            dismissProgress()
            if (loginInfo.loginTrackId != null) {
                loginParams.loginTrackId = loginInfo.loginTrackId
                loginTrackId.value = SingleEvent(loginInfo.loginTrackId!!)
            }
        }
    }

    /**
     * 注册轨迹绑定手机号
     */
    fun bindingMobileForTrack() {
        val phone = loginParams.phoneNumber
        val code = loginParams.authCode
        val loginTrackId = loginParams.loginTrackId

        if (phone.check("手机号码不能为空")) return
        if (code.check("验证码不能为空")) return
        if (loginTrackId.check("注册轨迹Id不能为空")) return

        viewModelScope.launch {
            val result: String? = try {
                requestCoroutineNotCatch {
                    loginRepository.bindingMobileForTrack(loginParams.toCodeLoginParams())
                }
            } catch (_: Exception) {
                "error"
            }
            if (result == "error") return@launch
            if (result.isNullOrEmpty()) {
                //跳转到邀请码填写页面继续流程
                this@LoginViewModel.loginTrackId.value = SingleEvent(loginTrackId!!)
            } else {
                //TODO 合并用户流程
            }
        }
    }

    /**
     * 请求验证码
     */
    fun requestCode() {
        val phone = loginParams.phoneNumber
        if (phone.check("手机号码不能为空")) return
        if (agreementChecked.value.check("请阅读并勾选用户协议选项")) return

        viewModelScope.launch {
            val success = requestCoroutine {
                loginRepository.requestCode(phone)
            } ?: false
            if (success) {
                toast("验证码发送成功")
                loginCodeEnableListener?.loginCodeEnable(false)
                startTimeInterval()
            }
        }
    }

    /**
     * 开启验证码倒计时
     */
    private suspend fun startTimeInterval() {
        for (index in 59 downTo 0) {
            codeString.postValue(if (index == 0) "验证码" else "$`index`s")
            delay(1000)
            if (index == 0) loginCodeEnableListener?.loginCodeEnable(true)
        }
    }

    /**
     * 用户协议勾选项
     */
    fun userArgeementStateChange(view: View) {
        view as TextView
        if (view.selectionStart != -1 || view.selectionEnd != -1) return
        agreementChecked.value = !(agreementChecked.value ?: false)
    }

    /**
     * 根据邀请码查询用户信息
     */
    fun queryUserByInvitationCode() {
        //是否跳过填写邀请码
        val invitationCode = loginParams.invitationCode
        val isDefault = invitationCode.isNullOrEmpty()
        viewModelScope.launch {
            val loginUserInfo: LoginUserInfo = requestCoroutine {
                val httpParam =
                    if (isDefault) HttpParam.obtain(
                        "defaultAllocation",
                        true
                    ) else HttpParam.obtain(
                        "invitationCode",
                        invitationCode!!
                    )
                loginRepository.queryUserByInvitationCode(httpParam)
            } ?: return@launch
            if (isDefault) {
                //默认，直接登录
                loginByInvitationCode()
            } else {
                //TODO 弹出用户信息，然后登录
            }
        }
    }

    /**
     * 邀请码登录
     */
    private fun loginByInvitationCode() {
        loginParams.apply {
            authType = LoginEnum.INVITATION_CODE
            inviteType = 0
            registerType = 0
            deviceNo = UMConfigure.getUMIDString(getTopActivity())
            defaultAllocation = invitationCode.isNullOrEmpty()
        }

        login()
    }

    override fun unInstalled() {
        toast("未安装微信！")
    }

    override fun loginSuccess(code: String) {
        loginByWechat(code)
    }

    override fun loginCancel() {
    }
}