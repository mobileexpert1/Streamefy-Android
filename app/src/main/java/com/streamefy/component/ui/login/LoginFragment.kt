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
import com.streamefy.data.KoinCompo
import com.streamefy.databinding.FragmentLoginBinding
import com.streamefy.utils.showMessage

class LoginFragment : BaseFragment<FragmentLoginBinding>(){
    var viewmodel = KoinCompo().loginVM
    override fun bindView(): Int = R.layout.fragment_login

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListeners()

    }


    private fun initClickListeners() = with(binding) {
        tvGetOtp.setOnClickListener{
            if (viewmodel.isValidFullName(binding.etFullname.text.toString())) {
                if (viewmodel.isValidPhoneNumberLength(binding.etPhoneNumber.text.toString())) {
                    findNavController().navigate(R.id.action_loginFragment_to_otpFragment)
                } else {
                    requireActivity().showMessage("Invalid Phone number!")
                }
            } else {
                requireActivity().showMessage("Please enter valid name")
            }
        }
    }

}