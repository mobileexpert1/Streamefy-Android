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
import com.streamefy.data.KoinCompo
import com.streamefy.data.KoinCompo.otpVm
import com.streamefy.databinding.FragmentLoginBinding
import com.streamefy.databinding.FragmentOtpBinding
import com.streamefy.network.MyResource
import com.streamefy.utils.setupNextFocusOnDigit

class OtpFragment : BaseFragment<FragmentOtpBinding>(), View.OnClickListener {
    var completeOtp = "000000"
    override fun bindView(): Int = R.layout.fragment_otp
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListeners()
        observeData()
    }

    private fun observeData() {
        otpVm.otpLiveData.observe(requireActivity()){
            when(it){
                is MyResource.isLoading->{}
                is MyResource.isSuccess->{}
                is MyResource.isError->{}
                else->{}
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
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.tvProceed -> {
                if (completeOtp.equals("000000")) {
                    findNavController().navigate(R.id.action_otpFragment_to_pinAuthenticationFragment)
                } else {
                    Toast.makeText(context, "Otp Verification Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}