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
import com.streamefy.databinding.FragmentLoginBinding

class LoginFragment : Fragment(), View.OnClickListener {

    private val binding: FragmentLoginBinding by lazy { FragmentLoginBinding.inflate(layoutInflater) }
    private lateinit var viewmodel: LoginViewmodel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        init()

    }

    private fun init() {
        viewmodel = ViewModelProvider(this).get(LoginViewmodel::class.java)
    }

    private fun initClickListeners() {
        binding.apply {
            tvGetOtp.setOnClickListener(this@LoginFragment)
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.tvGetOtp -> {
                if (viewmodel.isValidFullName(binding.etFullname.text.toString())) {
                    if (viewmodel.isValidPhoneNumberLength(binding.etPhoneNumber.text.toString())) {
                        findNavController().navigate(R.id.action_loginFragment_to_otpFragment)
                    } else {
                        Toast.makeText(context, "Invalid Phone number!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Please enter valid Phone number!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}