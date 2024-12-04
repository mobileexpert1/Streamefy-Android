package com.streamefy.component.ui.otp

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager

import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController

import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.otp.model.OTPRequest
import com.streamefy.component.ui.otp.model.VerificationRequest
import com.streamefy.component.ui.otp.viewmodel.OTPVM
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentOtpBinding
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.MyResource
import com.streamefy.utils.gone
import com.streamefy.utils.hideKey
import com.streamefy.utils.loadAny
import com.streamefy.utils.loadPicaso
import com.streamefy.utils.loadUrl
import com.streamefy.utils.previousFocusOnDigit
import com.streamefy.utils.remoteKey
import com.streamefy.utils.setupNextFocusOnDigit
import com.streamefy.utils.showKeyboard
import com.streamefy.utils.startCountdownTimer
import com.streamefy.utils.visible
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class OtpFragment : BaseFragment<FragmentOtpBinding>(), View.OnClickListener {
    var completeOtp = "000000"
    override fun bindView(): Int = R.layout.fragment_otp
    var phone: String = "6280830819"
    var name: String = "appdev096"
    var applogo = ""
    var app_background = ""

    var isResend = false
    private val viewModel: OTPVM by viewModel()
    override fun netStatus() {
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.run {
            phone = getString(PrefConstent.PHONE_NUMBER).toString()
        }
        name = SharedPref.getString(PrefConstent.FULL_NAME).toString()

        applogo = SharedPref.getString(PrefConstent.APP_LOGO).toString()
        app_background = SharedPref.getString(PrefConstent.AUTH_BACKGROUND).toString()
        binding.ivApplogo.loadAny(applogo)

        initClickListeners()
        binding.tvResend.clearFocus()
        otpFieldFocus()
        getOtp()

        observeData()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Show the custom dialog when back is pressed
                    findNavController().navigate(R.id.loginFragment)
                }
            })

    }

    private fun otpFieldFocus() = with(binding) {
//        tvProceed.remoteKey {
//            when(it){
//                StreamEnum.UP_DPAD_KEY->{
//                    tvResend.requestFocus()
//                }
//                else->{}
//            }
//        }

        otpView.remoteKey {
            when (it) {
                StreamEnum.UP_DPAD_KEY -> {
                    ivBack.requestFocus()
                }

                StreamEnum.DOWN_DPAD_KEY -> {
                    tvProceed.requestFocus()
                }

                else -> {

                }
            }
        }
        tvResend.remoteKey {
            when (it) {
                StreamEnum.UP_DPAD_KEY -> {
                    otpView.requestFocus()
                }

                StreamEnum.DOWN_DPAD_KEY -> {
                    tvProceed.requestFocus()
                }

                else -> {

                }
            }
        }

    }

    private fun startOtpTimer() = with(binding) {
        tvResend.isEnabled = false

        tvResend.startCountdownTimer(
            duration = 60 * 1000,
            onFinish = {
                val params = tvResend.layoutParams as ConstraintLayout.LayoutParams
                params.width =
                    requireActivity().resources.getDimensionPixelSize(R.dimen._30sdp) // Adjust to your desired size
                //  params.height = resources.getDimensionPixelSize(R.dimen._20sdp)
                tvResend.layoutParams = params
                tvremains.gone()
                tvResend.apply {
                    text = "Resend OTP"
                    isEnabled = true
                    setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                    //  requestFocus()
                }
            },
            onTick = { seconds ->

                val formattedSeconds = seconds.toString().padStart(2, '0')
                val params = tvResend.layoutParams as ConstraintLayout.LayoutParams
                params.width =
                    requireActivity().resources.getDimensionPixelSize(R.dimen._17sdp) // Adjust to your desired size
                tvResend.layoutParams = params

                if (formattedSeconds != "00") {
                    tvremains.visible()
                    tvResend.apply {
//                        text = "Resend OTP in 00:" + formattedSeconds
                        text = "00:" + formattedSeconds
                        isEnabled = false
                        if (isAdded) {
                            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        }
                        //clearFocus()
                    }
                }
            }
        )
    }
//
//
//    private fun startOtpTimer() = with(binding) {
//        tvResend.isEnabled = false
//
//        tvRemainNumber.startCountdownTimer(
//            duration = 60 * 1000,
//            onFinish = {
//                tvResend.visible()
//                  tvResend.gone()
//                tvResend.apply {
//                    text = "Resend OTP"
//                    isEnabled = true
//                    setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
//                    //  requestFocus()
//                }
//            },
//            onTick = { seconds ->
//
//                val formattedSeconds = seconds.toString().padStart(2, '0')
//                val params = tvResend.layoutParams as ConstraintLayout.LayoutParams
//                params.width =
//                    requireActivity().resources.getDimensionPixelSize(R.dimen._16sdp) // Adjust to your desired size
//                tvResend.layoutParams = params
//
//                if (formattedSeconds != "00") {
//                    tvResend.visible()
//                    tvResend.text="Resend OTP in 00:"
//
//                    tvRemainNumber.apply {
//                          visible()
//                        text = formattedSeconds
//                        isEnabled = false
//                        if (isAdded) {
//                            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
//                        }
//                        //clearFocus()
//                    }
//                }
//            }
//        )
//    }

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

            otpView.requestFocus()

            otpView.setOtpCompletionListener {
                // requireActivity().hideKey()
                tvProceed.requestFocus()
                requireActivity().hideKey()
            }
            otpView.cursorColor = ContextCompat.getColor(requireContext(), R.color.black)
//            otpView.addTextChangedListener {
//                var cursorIndex = otpView.selectionStart
//                Log.e("smskmc","$cursorIndex slxmskmc ${it.toString()}")
////                otpView.setCursorColor(ContextCompat.getColor(requireContext(),R.color.red))
//                otpView.cursorColor= ContextCompat.getColor(requireContext(),R.color.black)
////                otpView.setItemBackground(ContextCompat.getDrawable(requireContext(),R.drawable.indecator_bg))
////                otpView.otpViewItemCount
//            }

            otpView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    Log.e("smskmc", "$s before")
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    Log.e("smskmc", "$s onTextChanged")
                }

                override fun afterTextChanged(s: Editable?) {
                    Log.e("smskmc", "$s afterTextChanged")
                }
            })

            tvProceed.setOnClickListener(this@OtpFragment)
            ivBack.setOnClickListener(this@OtpFragment)

            otpView.setOnFocusChangeListener { v, hasFocus ->
                Log.e("smskmc", "$hasFocus setOnFocusChangeListener")
                if (hasFocus) {
                    otpView.setItemBackground(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_seleted_otp
                        )
                    )
                } else {
                    otpView.setItemBackground(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.bg_round_rect_stroke_gray
                        )
                    )

                }
            }


            otpView.setOnEditorActionListener { v, actionId, event ->
                Log.e("slcnslnc", "sjkcnbsakjbc otp screen ${otpView.text?.length}}")
                lifecycleScope.launch {
                    delay(100)
                    if (otpView.text != null) {
                        if (otpView.text?.isNotEmpty()!!) {
                            otpView.setSelection(otpView.text?.length!!)
                            otpView.jumpDrawablesToCurrentState()
                        } else {
                            otpView.setSelection(otpView.text?.length!!)
                        }
                    }
                }
                false
            }


//
            ivBack.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    val params = ivBack.layoutParams as ConstraintLayout.LayoutParams
                    params.width =
                        resources.getDimensionPixelSize(R.dimen._17sdp) // Adjust to your desired size
                    params.height = resources.getDimensionPixelSize(R.dimen._17sdp)
                    ivBack.layoutParams = params
                } else {
                    val params = ivBack.layoutParams as ConstraintLayout.LayoutParams
                    params.width = resources.getDimensionPixelSize(R.dimen._15sdp) // Original size
                    params.height = resources.getDimensionPixelSize(R.dimen._15sdp)
                    ivBack.layoutParams = params

                }

            }


            tvResend.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    tvResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
                    requireActivity().hideKey()
                } else {
                    tvResend.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            com.otpview.R.color.black
                        )
                    )
                }

            }

            tvResend.setOnClickListener {
                otpView.setText("")
                viewModel.getOtp(
                    requireActivity(),
                    OTPRequest(phone)
                )
            }


        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.tvProceed -> {
                binding.apply {
                    var otp = otpView.text.toString()
                    otp.run {
                        if (this.isEmpty()) {
                            ShowError.handleError.handleError(ErrorCodeManager.OTP_EMPTY)
                        } else if (this.length < 6) {
                            ShowError.handleError.handleError(ErrorCodeManager.OTP_LENGTH)
                        } else {
                            if (isAdded) {
                                viewModel.otpVerification(
                                    requireContext(),
                                    VerificationRequest(phone, this)
                                )
                                verificationObserv()
                            } else {
                                Log.e("otpfragment", "Fragment is not added, navigation aborted.")
                                onAttach(requireActivity())
                            }

                        }
                    }
                }
            }

            R.id.ivBack -> {
                findNavController().navigate(R.id.loginFragment)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    fun getOtp() {
        viewModel.getOtp(requireActivity(), OTPRequest(phone))
        viewModel.otpLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
                    showProgress()
                }

                is MyResource.isSuccess -> {
                    var data = it.data?.response
                    if (isResend) {
                        ShowError.handleError.message("OTP resent successfully")
                    } else {
                        ShowError.handleError.message(data.toString())
                    }
                    isResend = true
                    startOtpTimer()
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
                    showProgress()
                }

                is MyResource.isSuccess -> {
                    it.data?.run {
                        var data = this.response
                        if (isSuccess) {
                            ShowError.handleError.message(this.response.message)
                            lifecycleScope.launch {
                                SharedPref.setBoolean(
                                    PrefConstent.ISPRIMARY_USER,
                                    data.isPrimaryuser
                                )
                                SharedPref.setBoolean(PrefConstent.ISAUTH, false)
                                delay(2500)
                                var bundle = Bundle()
                                bundle.putString(PrefConstent.PHONE_NUMBER, phone)
                                bundle.putString(PrefConstent.FULL_NAME, name)
                                bundle.putBoolean(PrefConstent.ISHOME, false)
                                SharedPref.setBoolean(PrefConstent.ISAUTH, false)
                                if (data.email!=null) {
                                    SharedPref.setString(PrefConstent.USER_EMAIL, data.email!!)
                                }
                                if (response.isPrimaryuser) {
                                    findNavController().navigate(
                                        R.id.action_otpFragment_to_projectfragment,
                                        bundle
                                    )
                                } else {
                                    findNavController().navigate(
                                        R.id.action_otpFragment_to_pinAuthenticationFragment,
                                        bundle
                                    )
                                }
                            }
                        }
                    }

                }

                is MyResource.isError -> {
                    dismissProgress()
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

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("skcnmskncm", "skcnsk destroyview")
        dismissProgress()
    }
}