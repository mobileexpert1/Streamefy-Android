package com.streamefy.component.ui.pin_authentication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doBeforeTextChanged
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.otpview.OTPListener
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.otp.viewmodel.OTPVM
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentPinAuthenticationBinding
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.MyResource
import com.streamefy.utils.capitalizeFirstLetter
import com.streamefy.utils.hideKey
import com.streamefy.utils.loadPicaso
import com.streamefy.utils.loadUrl
import com.streamefy.utils.remoteKey
import org.koin.androidx.viewmodel.ext.android.viewModel


class PinAuthenticationFragment : BaseFragment<FragmentPinAuthenticationBinding>() {
    override fun bindView(): Int = R.layout.fragment_pin_authentication
    var phone = ""
    var otp = ""
    var applogo=""
    var app_background=""
    private val viewModel: PinVM by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SharedPref.setBoolean(PrefConstent.ISAUTH, false)
        arguments?.run {
            phone = getString(PrefConstent.PHONE_NUMBER).toString()
        }
        var name = SharedPref.getString(PrefConstent.FULL_NAME).toString()
        applogo=  SharedPref.getString(PrefConstent.APP_LOGO).toString()
       // app_background=SharedPref.getString(PrefConstent.AUTH_BACKGROUND).toString()
        binding.ivApplogo.loadPicaso(applogo)
        otpFieldFocus()
        binding.apply {
            textView2.setText("Welcome ${capitalizeFirstLetter(name)}! We are thrilled to have you here")

            // pinView.requestFocusOTP()
            // pinView.requestFocus()
//            pinView.otpListener = object : OTPListener {
//                override fun onInteractionListener() {
//                }
//                override fun onOTPComplete(otp: String) {
//                   // requireActivity().hideKey()
//                }
//            }
            ivBack.setOnClickListener {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.splashScreen, true)
                    .build()
                findNavController().navigate(R.id.loginFragment, null, navOptions)
            }
            ivBack.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    val params = ivBack.layoutParams as ConstraintLayout.LayoutParams
                    params.width =
                        resources.getDimensionPixelSize(R.dimen._20sdp) // Adjust to your desired size
                    params.height = resources.getDimensionPixelSize(R.dimen._20sdp)
                    ivBack.layoutParams = params
                } else {
                    val params = ivBack.layoutParams as ConstraintLayout.LayoutParams
                    params.width = resources.getDimensionPixelSize(R.dimen._15sdp) // Original size
                    params.height = resources.getDimensionPixelSize(R.dimen._15sdp)
                    ivBack.layoutParams = params

                }

            }
            tvProceed.setOnClickListener {
//                otp = et1.text.toString().trim() +
//                        et2.text.toString().trim() +
//                        et3.text.toString().trim() +
//                        et4.text.toString().trim()

                otp = otpView.text.toString()
                otp.run {
                    otp = this
                    if (otp.isEmpty()) {
                        ShowError.handleError.handleError(ErrorCodeManager.PIN_EMPTY)
                    } else if (otp.length < 4) {
                        ShowError.handleError.handleError(ErrorCodeManager.PIN_LENGTH)
                    } else {

//                    SharedPref.setBoolean(PrefConstent.ISLOGIN, true)
//                    SharedPref.setString(PrefConstent.AUTH_PIN, otp)
//                    findNavController().navigate(R.id.homefragment)

                        viewModel.setPin(requireActivity(), otp)
                        observe()
                    }
                }
            }
//            et1.setupNextFocusOnDigit(et2)
//            et2.setupNextFocusOnDigit(et3)
//            et3.setupNextFocusOnDigit(et4)
//
//            //previous
//            et4.previousFocusOnDigit(et3)
//            et3.previousFocusOnDigit(et2)
//            et2.previousFocusOnDigit(et1)
            otpView.requestFocus()
            otpView.setOtpCompletionListener {
                //requireActivity().hideKey()
                tvProceed.requestFocus()
            }
//
//            otpView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
//                if (hasFocus) {
//                    otpView.cursorColor = ContextCompat.getColor(requireContext(), R.color.black)
//                }
//            }
            otpView.cursorColor = ContextCompat.getColor(requireContext(), R.color.black)

            otpView.addTextChangedListener {
                var cursorIndex = otpView.selectionStart
                Log.e("smskmc", "$cursorIndex slxmskmc ${it.toString()}")
//                otpView.setCursorColor(ContextCompat.getColor(requireContext(),R.color.red))
                otpView.cursorColor = ContextCompat.getColor(requireContext(), R.color.black)
//                otpView.setItemBackground(ContextCompat.getDrawable(requireContext(),R.drawable.indecator_bg))
            }


            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        // Show the custom dialog when back is pressed
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(R.id.splashScreen, true)
                            .build()
                            findNavController().navigate(R.id.loginFragment, null, navOptions)
                    }
                })
        }
    }

    private fun otpFieldFocus() = with(binding) {
        et1.requestFocus()
        et1.remoteKey {
            when (it) {
                StreamEnum.LEFT_DPAD_KEY -> {
                    ivBack.requestFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                    et2.requestFocus()
                }
                StreamEnum.DOWN_DPAD_KEY -> {
                    tvProceed.requestFocus()
                }
                StreamEnum.UP_DPAD_KEY -> {
                    ivBack.requestFocus()
                } else -> {}
            }
        }
        et2.remoteKey {
            when (it) {
                StreamEnum.LEFT_DPAD_KEY -> {
                    et1.requestFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                    et3.requestFocus()
                }
                StreamEnum.DOWN_DPAD_KEY -> {
                    tvProceed.requestFocus()
                }

                StreamEnum.UP_DPAD_KEY -> {
                    ivBack.requestFocus()
                } else -> {}
            }
        }
        et3.remoteKey {
            when (it) {
                StreamEnum.LEFT_DPAD_KEY -> {
                    et2.requestFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                    et4.requestFocus()
                }
                StreamEnum.DOWN_DPAD_KEY -> {
                    tvProceed.requestFocus()
                }

                StreamEnum.UP_DPAD_KEY -> {
                    ivBack.requestFocus()
                } else -> {}
            }
        }
        et4.remoteKey {
            when (it) {
                StreamEnum.LEFT_DPAD_KEY -> {
                    et3.requestFocus()
                }

                StreamEnum.RIGHT_DPAD_KEY -> {
                    tvProceed.requestFocus()
                }
                StreamEnum.DOWN_DPAD_KEY -> {
                    tvProceed.requestFocus()
                }

                StreamEnum.UP_DPAD_KEY -> {
                    ivBack.requestFocus()
                }  else -> {}
            }
        }

        otpView.setOnFocusChangeListener { v, hasFocus ->
            Log.e("smskmc", "$hasFocus setOnFocusChangeListener")
            if (hasFocus) {
                otpView.setItemBackground(ContextCompat.getDrawable(requireContext(),R.drawable.ic_selected_inputfiled))
            }else{
                otpView.setItemBackground(ContextCompat.getDrawable(requireContext(),R.drawable.bg_round_stroke_gray))

            }
        }


//        et1.setupNextFocusOnDigit(et2)
//        et2.setupNextFocusOnDigit(et3)
//        et3.setupNextFocusOnDigit(et4)
//        et4.setupNextFocusOnDigit(et5)
//        et5.setupNextFocusOnDigit(et6)
//
//        // previous
//
//        et6.previousFocusOnDigit(et5)
//        et5.previousFocusOnDigit(et4)
//        et4.previousFocusOnDigit(et3)
//        et3.previousFocusOnDigit(et2)
//        et2.previousFocusOnDigit(et1)

    }



    private fun observe() {
        viewModel.pinData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
                    showProgress()
                }
//                    val navOptions = NavOptions.Builder()
////                        .setPopUpTo(R.id.splashScreen, true)
////                        .setPopUpTo(R.id.loginFragment,true)
////                        .setPopUpTo(R.id.otpFragment,true)
//                            .setPopUpTo(R.id.pinAuthenticationFragment, true)
//                            // .setLaunchSingleTop(true)
//                            // Set inclusive to true
//                            .build()
//                        // Navigate to home fragment with the options
//                        findNavController().navigate(R.id.homefragment, null, navOptions)
                is MyResource.isSuccess -> {
                    SharedPref.setBoolean(PrefConstent.ISLOGIN, true)
                    SharedPref.setString(PrefConstent.AUTH_PIN, otp)
                    Log.e("sjxbjsbc","ksjnckjanc ${it.data}")
//                    findNavController().navigate(R.id.homefragment)
                    if (isAdded) {
                        findNavController().navigate(R.id.homefragment)

                    }
                }

                is MyResource.isError -> {
                    dismissProgress()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("skcnmskncm","skcnsk destroyview")
        dismissProgress()
    }
}