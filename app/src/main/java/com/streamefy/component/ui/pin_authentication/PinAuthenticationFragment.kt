package com.streamefy.component.ui.pin_authentication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.ui.otp.model.VerificationRequest
import com.streamefy.data.KoinCompo
import com.streamefy.data.KoinCompo.homeVm
import com.streamefy.data.KoinCompo.pinVm
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentPinAuthenticationBinding
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.Constants
import com.streamefy.network.MyResource
import com.streamefy.utils.setupNextFocusOnDigit

class PinAuthenticationFragment : BaseFragment<FragmentPinAuthenticationBinding>() {
    override fun bindView(): Int = R.layout.fragment_pin_authentication
    var phone = ""
    var otp = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SharedPref.setBoolean(PrefConstent.ISAUTH, false)
        arguments?.run {
            phone = getString(PrefConstent.PHONE_NUMBER).toString()
        }
        binding.apply {

            tvProceed.setOnClickListener {
                otp = et1.text.toString().trim() +
                        et2.text.toString().trim() +
                        et3.text.toString().trim() +
                        et4.text.toString().trim()

                if (otp.isEmpty()) {
                    ShowError.handleError.handleError(ErrorCodeManager.OTP_EMPTY)
                } else if (otp.length < 4) {
                    ShowError.handleError.handleError(ErrorCodeManager.OTP_LENGTH)
                } else {
                    pinVm.setPin(requireActivity(), 1, 1, otp, phone)
                    observe()
                }

            }
            et1.setupNextFocusOnDigit(et2)
            et2.setupNextFocusOnDigit(et3)
            et3.setupNextFocusOnDigit(et4)
        }
        Log.e("sjanckjan", "cnjd ${SharedPref.getString(PrefConstent.TOKEN)}")

    }

    private fun observe() {
        pinVm.pinData.observe(requireActivity()) {
            when (it) {
                is MyResource.isLoading -> {
                    showProgress()
                }

                is MyResource.isSuccess -> {
                    SharedPref.setBoolean(PrefConstent.ISLOGIN, true)
                    SharedPref.setString(PrefConstent.AUTH_PIN, otp)
                    findNavController().navigate(R.id.homefragment)
                    dismissProgress()
                }

                is MyResource.isError -> {
                    dismissProgress()
                }
            }
        }
    }

}