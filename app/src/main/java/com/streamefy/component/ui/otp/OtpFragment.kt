package com.streamefy.component.ui.otp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.ui.login.model.LoginRequest
import com.streamefy.component.ui.otp.model.OTPRequest
import com.streamefy.component.ui.otp.model.VerificationRequest
import com.streamefy.data.KoinCompo
import com.streamefy.data.KoinCompo.otpVm
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentLoginBinding
import com.streamefy.databinding.FragmentOtpBinding
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.MyResource
import com.streamefy.utils.previousFocusOnDigit
import com.streamefy.utils.setupNextFocusOnDigit
import com.streamefy.utils.showMessage

class OtpFragment : BaseFragment<FragmentOtpBinding>(), View.OnClickListener {
    var completeOtp = "000000"
    override fun bindView(): Int = R.layout.fragment_otp
    var phone: String = "6280830819"
    var name: String = "appdev096"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.run {
            phone = getString(PrefConstent.PHONE_NUMBER).toString()
        }
        name=SharedPref.getString(PrefConstent.FULL_NAME).toString()
        initClickListeners()

        getOtp()

        observeData()
    }

    private fun observeData() {
        otpVm.otpLiveData.observe(requireActivity()) {
            when (it) {
                is MyResource.isLoading -> {}
                is MyResource.isSuccess -> {}
                is MyResource.isError -> {}
                else -> {}
            }
        }
    }

    private fun initClickListeners() {
        binding.apply {
            tvProceed.setOnClickListener(this@OtpFragment)
            et1.setupNextFocusOnDigit(et2)
            et2.setupNextFocusOnDigit(et3)
            et3.setupNextFocusOnDigit(et4)
            et4.setupNextFocusOnDigit(et5)
            et5.setupNextFocusOnDigit(et6)

            // previous

            et6.previousFocusOnDigit(et5)
            et5.previousFocusOnDigit(et4)
            et4.previousFocusOnDigit(et3)
            et3.previousFocusOnDigit(et2)
            et2.previousFocusOnDigit(et1)

        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.tvProceed -> {
                binding.apply {

                    var otp = et1.text.toString().trim() +
                            et2.text.toString().trim() +
                            et3.text.toString().trim() +
                            et4.text.toString().trim() +
                            et5.text.toString().trim() +
                            et6.text.toString().trim()

                    if (otp.isEmpty()) {
                        ShowError.handleError.handleError(ErrorCodeManager.OTP_EMPTY)
                    } else if (otp.length < 6) {
                        ShowError.handleError.handleError(ErrorCodeManager.OTP_LENGTH)
                    } else {
                        otpVm.otpVerification(
                            requireActivity(),
                            VerificationRequest(phone, otp)
                        )
                        verificationObserv()

//                            var bundle=Bundle()
//                            bundle.putString(PrefConstent.PHONE_NUMBER,phone)
//                            bundle.putString(PrefConstent.FULL_NAME,name)
//                            SharedPref.setBoolean(PrefConstent.ISAUTH,false)
//                            findNavController().navigate(R.id.action_otpFragment_to_pinAuthenticationFragment,bundle)


                    }
                }
            }
        }
    }


    fun getOtp() {
        otpVm.getOtp(
            requireActivity(),
            OTPRequest(name, phone)
        )

        otpVm.otpLiveData.observe(requireActivity()) {
            when (it) {
                is MyResource.isLoading -> {
                    ///loading
                    showProgress()
                }

                is MyResource.isSuccess -> {
                    var data = it.data?.response
                    dismissProgress()
                }

                is MyResource.isError -> {
                    dismissProgress()
                }

            }
        }
    }

    fun verificationObserv() {
        otpVm.vericationData.observe(requireActivity()) {
            when (it) {
                is MyResource.isLoading -> {
                    ///loading
                    showProgress()
                }

                is MyResource.isSuccess -> {
                    it.data?.run {
                        if (isSuccess) {
                            var bundle = Bundle()
                            bundle.putString(PrefConstent.PHONE_NUMBER, phone)
                            bundle.putString(PrefConstent.FULL_NAME, name)
                            SharedPref.setBoolean(PrefConstent.ISAUTH, false)

                            findNavController().navigate(
                                R.id.action_otpFragment_to_pinAuthenticationFragment,
                                bundle
                            )

                        }
                    }
                    dismissProgress()
                }

                is MyResource.isError -> {
                    dismissProgress()
                    requireActivity().showMessage(it.error)
                }

            }
        }

    }

}