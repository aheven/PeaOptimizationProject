package com.mall.login.fragment

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mall.libcommon.base.BaseFragment
import com.mall.libcommon.ext.loge
import com.mall.login.databinding.LoginFragmentTrackBindPhoneBinding
import com.mall.login.listener.LoginCodeEnableListener
import com.mall.login.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * 注册轨迹绑定手机号页
 */
@AndroidEntryPoint
class LoginTrackBindPhoneFragment : BaseFragment<LoginFragmentTrackBindPhoneBinding>(),
    LoginCodeEnableListener {
    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun initView() {
    }

    override fun initData() {
        binding.lifecycleOwner = this
        binding.viewmodel = loginViewModel
        //跳转到邀请码填写页面
        loginViewModel.loginTrackId.observe(viewLifecycleOwner, { singleEvent ->
            singleEvent.getContentIfNotHandled()?.let {
                intentInvitationCodeFragment()
            }
        })

        initListener()
    }

    /**
     * 跳转到邀请码填写页面
     */
    private fun intentInvitationCodeFragment() {
        val navDirection =
            LoginTrackBindPhoneFragmentDirections.actionLoginTrackBindPhoneFragmentToLogIninvitationCodefragment()
        findNavController().navigate(navDirection)
    }

    private fun initListener() {
        loginViewModel.setLoginCodeEnableListener(this)
    }

    override fun loginCodeEnable(enable: Boolean) {
        binding.verificationCode.isClickable = enable
    }
}