package com.streamefy.component.ui.otp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View

import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController

import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.ui.otp.model.OTPRequest
import com.streamefy.component.ui.otp.model.VerificationRequest
import com.streamefy.component.ui.otp.viewmodel.OTPVM
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentOtpBinding
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.MyResource
import com.streamefy.utils.hideKey
import org.koin.androidx.viewmodel.ext.android.viewModel
class OtpFragment : BaseFragment<FragmentOtpBinding>(), View.OnClickListener {
    var completeOtp = "000000"
    override fun bindView(): Int = R.layout.fragment_otp
    var phone: String = "6280830819"
    var name: String = "appdev096"

    private val viewModel: OTPVM by viewModel()

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
        viewModel.otpLiveData.observe(requireActivity()) {
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
           // requireActivity().showKeyboard(otpView.getChildAt(0))
//            otpView.requestFocusOTP()
//            otpView.otpListener = object : OTPListener {
//                override fun onInteractionListener() {
//                    Log.e("skmkscn","scklknc")
//                   // tvProceed.clearFocus()
//                }
//
//                override fun onOTPComplete(otp: String) {
////                    tvProceed.requestFocus()
////                    tvProceed.isFocusableInTouchMode=true
//                    requireActivity().hideKey()
//                    Log.e("skmkscn","complete $otp")
//                }
//
//            }
//            otpView.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
//                if (event.action==KeyEvent.ACTION_DOWN){
//                    when(keyCode){
//                        KeyEvent.KEYCODE_ENTER->{
//                            Log.e("sncncsc","sjcnsjc enter")
//                        }
//                    }
//                }
//                false})
            otpView.requestFocus()

            otpView.setOtpCompletionListener {
                requireActivity().hideKey()
            }
            otpView.cursorColor= ContextCompat.getColor(requireContext(),R.color.black)
            otpView.addTextChangedListener {
                var cursorIndex = otpView.selectionStart
                Log.e("smskmc","$cursorIndex slxmskmc ${it.toString()}")
//                otpView.setCursorColor(ContextCompat.getColor(requireContext(),R.color.red))
                otpView.cursorColor= ContextCompat.getColor(requireContext(),R.color.black)
//                otpView.setItemBackground(ContextCompat.getDrawable(requireContext(),R.drawable.indecator_bg))
//                otpView.otpViewItemCount
            }
            tvProceed.setOnClickListener(this@OtpFragment)
            ivBack.setOnClickListener(this@OtpFragment)



//            et1.setupNextFocusOnDigit(et2)
//            et2.setupNextFocusOnDigit(et3)
//            et3.setupNextFocusOnDigit(et4)
//            et4.setupNextFocusOnDigit(et5)
//            et5.setupNextFocusOnDigit(et6)
//
//            // previous
//
//            et6.previousFocusOnDigit(et5)
//            et5.previousFocusOnDigit(et4)
//            et4.previousFocusOnDigit(et3)
//            et3.previousFocusOnDigit(et2)
//            et2.previousFocusOnDigit(et1)
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

            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        // Show the custom dialog when back is pressed
                        findNavController().navigateUp()
                    }
                })
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.tvProceed -> {
                binding.apply {

//                    var otp =
//                        et1.text.toString().trim() +
//                            et2.text.toString().trim() +
//                            et3.text.toString().trim() +
//                            et4.text.toString().trim() +
//                            et5.text.toString().trim() +
//                            et6.text.toString().trim()
                   var otp=otpView.text.toString()
                    otp.run {
                    if (this.isEmpty()) {
                        ShowError.handleError.handleError(ErrorCodeManager.OTP_EMPTY)
                       // tvProceed.clearFocus()
                    } else if (this.length < 6) {
                        ShowError.handleError.handleError(ErrorCodeManager.OTP_LENGTH)
                       // tvProceed.clearFocus()

                    } else {
                        if (isAdded) {
                            viewModel.otpVerification(
                                requireContext(),
                                VerificationRequest(phone, this)
                            )
                            verificationObserv()
                        }else{
                            Log.e("otpfragment", "Fragment is not added, navigation aborted.")
                            onAttach(requireActivity())
                        }

//                            var bundle=Bundle()
//                            bundle.putString(PrefConstent.PHONE_NUMBER,phone)
//                            bundle.putString(PrefConstent.FULL_NAME,name)
//                            SharedPref.setBoolean(PrefConstent.ISAUTH,false)
//                            findNavController().navigate(R.id.action_otpFragment_to_pinAuthenticationFragment,bundle)
                    }}
                }
            }
            R.id.ivBack->{
                findNavController().popBackStack()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
    fun getOtp() {
        viewModel.getOtp(
            requireActivity(),
            OTPRequest(name, phone)
        )

        viewModel.otpLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
                    ///loading
                    showProgress()
                }

                is MyResource.isSuccess -> {
                    var data = it.data?.response
                    ShowError.handleError.message(data.toString())
                    dismissProgress()
                }

                is MyResource.isError -> {
                    dismissProgress()
                }

            }
        }
    }

    fun verificationObserv() {
        viewModel.vericationData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
                    ///loading
                    showProgress()
                }

                is MyResource.isSuccess -> {
                    it.data?.run {
                        if (isSuccess) {
                            ShowError.handleError.message(this.response)
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
                   // requireActivity().showMessage(it.error)
                }

            }
        }

    }


    override fun onResume() {
        super.onResume()

        binding.apply {
           otpView.setText("")
        }

    }

}