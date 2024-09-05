package com.streamefy.component.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.ui.login.model.LoginRequest
import com.streamefy.data.KoinCompo
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentLoginBinding
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.MyResource
import com.streamefy.utils.LogMessage
import com.streamefy.utils.nameAndPassword
import com.streamefy.utils.nameValidation
import com.streamefy.utils.nameWithNumber
import com.streamefy.utils.showMessage

class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    var viewmodel = KoinCompo.loginVM
    override fun bindView(): Int = R.layout.fragment_login

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListeners()
//        ShowError.handleError.handleError(ErrorCodeManager.LOGIN_FAIL)
    }

    private fun initClickListeners() = with(binding) {
        tvGetOtp.setOnClickListener {
            var validate =
//                nameAndPassword("appsdev096@gmail.com", "Appsdev096#")
                nameWithNumber(etFullname.text.toString(), etPhoneNumber.text.toString())
            LogMessage.logeMe(validate.toString())
            if (!validate.equals(true)) {
                ShowError.handleError.handleError(validate as Int)
            } else {
                SharedPref.setBoolean(PrefConstent.ISAUTH,true)

//                viewmodel.login(
//                    requireActivity(),
//                    LoginRequest("appsdev096@gmail.com", "Appsdev096#")
//                )
//                observe()

                var bundle=Bundle()
                bundle.putString(PrefConstent.PHONE_NUMBER,binding.etPhoneNumber.text.toString())
                bundle.putString(PrefConstent.FULL_NAME,binding.etFullname.text.toString())
                findNavController().navigate(R.id.action_loginFragment_to_otpFragment,bundle)

            }
        }
    }


    fun observe() {
        viewmodel.loginLiveData.observe(requireActivity()) {
            when (it) {
                is MyResource.isLoading -> {
                    ///loading
                }
                is MyResource.isSuccess -> {
                    var data= it.data?.response
                    data?.run {
                        SharedPref.setString(PrefConstent.TOKEN,accessToken)
                        SharedPref.setString(PrefConstent.REFRESH_TOKEN,refreshToken)
                        SharedPref.setBoolean(PrefConstent.ISLOGIN,true)
                    }
                    var bundle=Bundle()
                    bundle.putString(PrefConstent.PHONE_NUMBER,binding.etPhoneNumber.text.toString())
                    findNavController().navigate(R.id.action_loginFragment_to_otpFragment,bundle)
                }
                is MyResource.isError -> {}
                else -> {}
            }
        }
    }

}