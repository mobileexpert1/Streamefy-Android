package com.streamefy.component.ui.otp

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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
import com.streamefy.utils.loadUrl
import com.streamefy.utils.remoteKey
import com.streamefy.utils.startCountdownTimer
import com.streamefy.utils.visible
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class OtpFragment : BaseFragment<FragmentOtpBinding>(), View.OnClickListener {
    override fun bindView(): Int = R.layout.fragment_otp
    var phone: String = ""
    var applogo = ""
    var app_background = ""

    var isResend = false
    var isDark=false
    private val viewModel: OTPVM by viewModel()
    override fun netStatus() {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.run {
            phone = getString(PrefConstent.PHONE_NUMBER).toString()
        }
         isDark=   SharedPref.getBoolean(PrefConstent.IS_DARK)
        applogo = SharedPref.getString(PrefConstent.APP_LOGO).toString()
        app_background = SharedPref.getString(PrefConstent.AUTH_BACKGROUND).toString()
        binding.ivbackground.loadUrl(app_background)
//        binding.ivApplogo.loadAny(applogo)
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
        resetColor()
    }

    private fun resetColor()= with(binding) {
//      Handle themes
        if (isDark) {
            constraintLayout.isEnabled=true
            tvInstruction.isEnabled=true
            tvremains.isEnabled=true
        } else {
            constraintLayout.isEnabled=false
            tvInstruction.isEnabled=false
            tvremains.isEnabled=false
        }
    }

//        Handle view focus and its key movement
    private fun otpFieldFocus() = with(binding) {
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
        tvResend.setOnFocusChangeListener { v, hasFocus ->
        if (hasFocus) {
            tvResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
            requireActivity().hideKey()
        } else {
            if (isDark) {
                tvResend.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
            }else{
                tvResend.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
            }
        }

    }
        otpView.setOnFocusChangeListener { v, hasFocus ->
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

    }

//    Handle reset OTP functionality
    private fun startOtpTimer() = with(binding) {
        tvResend.isEnabled = false
        tvResend.startCountdownTimer(
            duration = 60 * 1000,
            onFinish = {
                val params = tvResend.layoutParams as ConstraintLayout.LayoutParams
                params.width =
                    requireActivity().resources.getDimensionPixelSize(R.dimen._30sdp)
                //  params.height = resources.getDimensionPixelSize(R.dimen._20sdp)
                tvResend.layoutParams = params
                tvremains.gone()
                tvResend.apply {
                    text = "Resend OTP"
                    isEnabled = true
                    if (isDark) {
                        setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
                    }else{
                        setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                    }

                }
            },
            onTick = { seconds ->

                val formattedSeconds = seconds.toString().padStart(2, '0')
                val params = tvResend.layoutParams as ConstraintLayout.LayoutParams
                params.width =
                    requireActivity().resources.getDimensionPixelSize(R.dimen._17sdp)
                tvResend.layoutParams = params

                if (formattedSeconds != "00") {
                    tvremains.visible()
                    tvResend.apply {
                        text = "00:" + formattedSeconds
                        isEnabled = false
                        if (isAdded) {
                            if (isDark) {
                                setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
                            }else{
                                setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                            }                        }
                    }
                }
            }
        )
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
            otpView.requestFocus()
            otpView.setOtpCompletionListener {
                tvProceed.requestFocus()
                requireActivity().hideKey()
            }
            otpView.cursorColor = ContextCompat.getColor(requireContext(), R.color.black)
            tvProceed.setOnClickListener(this@OtpFragment)
            ivBack.setOnClickListener(this@OtpFragment)
            otpView.setOnEditorActionListener { v, actionId, event ->
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
                    val otp = otpView.text.toString()
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
//     get otp and resent otp response
    private fun getOtp() {
        viewModel.getOtp(requireActivity(), OTPRequest(phone))
        viewModel.otpLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
                    showProgress()
                }

                is MyResource.isSuccess -> {
                    val data = it.data?.response
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

//    get verification result and navigate to the Project Screen
    private fun verificationObserv() {
        viewModel.vericationData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
                    showProgress()
                }

                is MyResource.isSuccess -> {
                    it.data?.run {
                        val data = this.response
                        if (isSuccess) {
                            ShowError.handleError.message(this.response.message)
                            lifecycleScope.launch {
                                SharedPref.setBoolean(
                                    PrefConstent.ISPRIMARY_USER,
                                    data.isPrimaryuser
                                )
                                delay(2500)
                                val bundle = Bundle()
                                bundle.putString(PrefConstent.PHONE_NUMBER, phone)
                                bundle.putBoolean(PrefConstent.ISHOME, false)
                                SharedPref.setBoolean(PrefConstent.ISAUTH, false)
                                //** code updated
                                SharedPref.setBoolean(PrefConstent.ISLOGIN, true)
                                if (data.email != null) {
                                    SharedPref.setString(PrefConstent.USER_EMAIL, data.email)
                                }
                                findNavController().navigate(
                                    R.id.action_otpFragment_to_projectfragment,
                                    bundle
                                )
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
        dismissProgress()
    }
}