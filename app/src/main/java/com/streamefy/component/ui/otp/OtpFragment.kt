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
import com.streamefy.databinding.FragmentLoginBinding
import com.streamefy.databinding.FragmentOtpBinding

class OtpFragment :BaseFragment<FragmentOtpBinding>(), View.OnClickListener {

   // private val binding: FragmentOtpBinding by lazy { FragmentOtpBinding.inflate(layoutInflater) }
   var completeOtp="000000"

    override fun bindView(): Int =R.layout.fragment_otp

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()

    }

    private fun initClickListeners() {
        binding.apply {
            tvProceed.setOnClickListener(this@OtpFragment)
        }
    }

    private fun handleMultipleEditText() {

        val otpEditTexts = arrayOf(
            binding.et1,
            binding.et2,
            binding.et3,
            binding.et4,
            binding.et5,
            binding.et6
        )

        for (i in 0 until otpEditTexts.size - 1) {
            otpEditTexts[i].addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        otpEditTexts[i + 1].requestFocus()
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        // Handle backspace to move to previous EditText
        for (i in 1 until otpEditTexts.size) {
            otpEditTexts[i].setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN && otpEditTexts[i].text.isEmpty()) {
                    otpEditTexts[i - 1].requestFocus()
                    true
                } else {
                    false
                }
            }
        }

        // Get the complete OTP when needed
        completeOtp = otpEditTexts.joinToString("") { it.text.toString() }

    }


    override fun onClick(p0: View?) {
        when(p0?.id) {
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