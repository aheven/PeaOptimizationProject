package com.mall.login.fragment

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mall.libcommon.base.BaseFragment
import com.mall.login.databinding.LoginFragmentCodeBinding
import com.mall.login.listener.LoginCodeEnableListener
import com.mall.login.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * 手机验证码登录页
 */
@AndroidEntryPoint
class LoginByCodeFragment : BaseFragment<LoginFragmentCodeBinding>(), LoginCodeEnableListener {
    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun initView() {
    }

    override fun initData() {
        binding.lifecycleOwner = this
        binding.viewmodel = loginViewModel

        //跳转手机注册页
        loginViewModel.loginTrackId.observe(viewLifecycleOwner, { singleEvent ->
            singleEvent.getContentIfNotHandled()?.let {
                intentLoginTrackBindPhonePager()
            }
        })
        initListener()
    }

    private fun initListener() {
        loginViewModel.setLoginCodeEnableListener(this)
    }

    /**
     * 跳转到注册轨迹绑定手机号页
     */
    private fun intentLoginTrackBindPhonePager() {
        val navDirection =
            LoginByCodeFragmentDirections.actionLoginByCodeFragmentToLoginTrackBindPhoneFragment()
        findNavController().navigate(navDirection)
    }

    override fun loginCodeEnable(enable: Boolean) {
        binding.verificationCode.isClickable = enable
    }
}