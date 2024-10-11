package com.streamefy.component.ui.login

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.TextView.OnEditorActionListener
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.streamefy.MainActivity
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.login.model.LoginRequest
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentLoginBinding
import com.streamefy.network.MyResource
import com.streamefy.utils.LogMessage
import com.streamefy.utils.nameWithNumber
import com.streamefy.utils.remoteKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    //    var viewmodel = KoinCompo.loginVM
    val viewmodel: LoginViewmodel by viewModel()
    override fun bindView(): Int = R.layout.fragment_login

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListeners()
//        requireActivity().onBackPressedDispatcher.addCallback {
//            MainActivity().exitApp()
//        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            (requireActivity() as MainActivity).exitApp()
        }

//        ShowError.handleError.handleError(ErrorCodeManager.LOGIN_FAIL)
    }

    private fun initClickListeners() = with(binding) {
        tvGetOtp.setOnClickListener {
            var validate = nameWithNumber(etFullname.text.toString(), etPhoneNumber.text.toString())
            LogMessage.logeMe(validate.toString())
            if (validate) {
                // ShowError.handleError.handleError(validate as Int)
                //   } else {
                SharedPref.setString(PrefConstent.TOKEN, "")
                if (isAdded) {
                    viewmodel.login(
                        requireActivity(),
                        LoginRequest("appsdev096@gmail.com", "Appsdev096#")
                    )
                    observe()

//                    var bundle = Bundle()
//                    bundle.putString(
//                        PrefConstent.PHONE_NUMBER,
//                        binding.etPhoneNumber.text.toString()
//                    )
//                    bundle.putString(PrefConstent.FULL_NAME, binding.etFullname.text.toString())
//                    findNavController().navigate(R.id.otpFragment, bundle)

                } else {
                    onAttach(requireActivity())
                    Log.e("login fragment", "Fragment is not added, navigation aborted.")
                }

//

            }
        }

        etFullname.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                if (etFullname.text.isNotEmpty()) {
                    etFullname.setSelection(etFullname.text.length)
                }
            }
        }
        etPhoneNumber.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                if (etPhoneNumber.text.isNotEmpty()) {
                    etPhoneNumber.setSelection(etPhoneNumber.text.length)
                }
            }
        }
        etPhoneNumber.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        tvGetOtp.requestFocus()
                        return@OnKeyListener true
                    }

                    KeyEvent.KEYCODE_DPAD_UP -> {
                        etFullname.requestFocus()
                        return@OnKeyListener true
                    }
                }
            }
            false
        })

        tvGetOtp.remoteKey {
            when (it) {
                StreamEnum.UP_DPAD_KEY -> {
                    etPhoneNumber.requestFocus()
                }

                else -> {}
            }
        }
        etFullname.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        etPhoneNumber.requestFocus()
                        return@OnKeyListener true
                    }
                }
            }
            false
        })


        etFullname.setOnEditorActionListener { v, actionId, event ->
            Log.e("slcnslnc", "sjkcnbsakjbc setOnEditorActionListener ${etFullname.text.length}")
            lifecycleScope.launch {
                delay(100)
                if (etFullname.text.isNotEmpty()) {
                    etFullname.setSelection(etFullname.text.length)
                } else {
                    etFullname.setSelection(etFullname.text.length)
                }
                if (etPhoneNumber.text.isNotEmpty()) {
                    etPhoneNumber.setSelection(etPhoneNumber.text.length)
                }
            }
            false
        }
        etPhoneNumber.setOnEditorActionListener { v, actionId, event ->
            Log.e("slcnslnc", "sjkcnbsakjbc setOnEditorActionListener ${etFullname.text.length}")
            lifecycleScope.launch {
                delay(100)
                if (etPhoneNumber.text.isNotEmpty()) {
                    etPhoneNumber.setSelection(etPhoneNumber.text.length)
                    tvGetOtp.requestFocus()
                } else {
                    etPhoneNumber.setSelection(etPhoneNumber.text.length)
                }

            }
            false
        }

    }

    override fun onResume() {
        super.onResume()
        Log.e("slcnslnc", "onResume")
        binding.apply {
            etFullname.setText("")
            etPhoneNumber.setText("")
        }

    }

    fun observe() {
        viewmodel.loginLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
                    ///loading
                    progressDialog.show()
                }

                is MyResource.isSuccess -> {
                    progressDialog.dismiss()
                    var data = it.data?.response
                    data?.run {
                        SharedPref.setString(PrefConstent.TOKEN, accessToken)
                        SharedPref.setString(PrefConstent.REFRESH_TOKEN, refreshToken)
                        SharedPref.setString(
                            PrefConstent.PHONE_NUMBER,
                            binding.etPhoneNumber.text.toString()
                        )
                        SharedPref.setString(
                            PrefConstent.FULL_NAME,
                            binding.etFullname.text.toString()
                        )

                        SharedPref.setString(
                            PrefConstent.APP_LOGO,
                           data.logo
                        )
//                        data.profileImage?.run {
//                            SharedPref.setString(
//                                PrefConstent.AUTH_BACKGROUND,
//                                data.profileImage
//                            )
//                        }


//                        SharedPref.setBoolean(PrefConstent.ISLOGIN,true)
                    }
                    var bundle = Bundle()
                    bundle.putString(
                        PrefConstent.PHONE_NUMBER,
                        binding.etPhoneNumber.text.toString()
                    )
                    if (isAdded) {
                        findNavController().navigate(R.id.otpFragment, bundle)
                    } else {
                        progressDialog.dismiss()
                    }
                }

                is MyResource.isError -> {
                    progressDialog.dismiss()
                }
            }
        }
    }

}