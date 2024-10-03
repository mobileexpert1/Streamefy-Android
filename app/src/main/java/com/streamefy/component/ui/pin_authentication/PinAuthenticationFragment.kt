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
import com.streamefy.data.KoinCompo.pinVm
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentPinAuthenticationBinding
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.MyResource
import com.streamefy.utils.capitalizeFirstLetter
import com.streamefy.utils.hideKey


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
        var name = SharedPref.getString(PrefConstent.FULL_NAME).toString()


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
                findNavController().popBackStack()
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

                        pinVm.setPin(requireActivity(), 1, 1, otp, phone)
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
                requireActivity().hideKey()
            }

            otpView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    otpView.cursorColor = ContextCompat.getColor(requireContext(), R.color.black)
                }
            }
            otpView.cursorColor = ContextCompat.getColor(requireContext(), R.color.black)

            otpView.addTextChangedListener {
                var cursorIndex = otpView.selectionStart
                Log.e("smskmc", "$cursorIndex slxmskmc ${it.toString()}")
//                otpView.setCursorColor(ContextCompat.getColor(requireContext(),R.color.red))
                otpView.cursorColor = ContextCompat.getColor(requireContext(), R.color.black)
//                otpView.setItemBackground(ContextCompat.getDrawable(requireContext(),R.drawable.indecator_bg))
            }


//            otpView.addTextChangedListener(object : TextWatcher {
//                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                    // No action needed here
//                    otpView.cursorColor=ContextCompat.getColor(requireContext(),R.color.red)
//                }
//
//                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    // Get the current cursor position
//                    val cursorIndex = otpView.selectionStart
//
//                    otpView.setCursorColor(ContextCompat.getColor(requireContext(),R.color.red))
//                    otpView.cursorColor=ContextCompat.getColor(requireContext(),R.color.red)
////                    otpView.setItemBackgroundResources(R.drawable.bg_round_gradient_purple_orange)
//                    //otpView.setItemBackground(ContextCompat.getDrawable(requireContext(),R.drawable.bg_round_gradient_purple_orange))
//
//                    // Set the cursor color
//                }
//
//                override fun afterTextChanged(s: Editable?) {
//
//                }
//            })

            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        // Show the custom dialog when back is pressed
                        findNavController().popBackStack()
                    }
                })
        }
    }

    private fun observe() {
        pinVm.pinData.observe(viewLifecycleOwner) {
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
                        dismissProgress()
                    }
                }

                is MyResource.isError -> {
                    dismissProgress()
                }
            }
        }
    }

}