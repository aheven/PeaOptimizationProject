package com.mall.login.fragment

import androidx.fragment.app.activityViewModels
import com.mall.libcommon.base.BaseFragment
import com.mall.login.databinding.LoginFragmentInvatationCodeBinding
import com.mall.login.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * 邀请码填写页面
 */
@AndroidEntryPoint
class LoginInvitationCodeFragment : BaseFragment<LoginFragmentInvatationCodeBinding>() {
    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun initView() {
    }

    override fun initData() {
        binding.viewmodel = loginViewModel
    }
}