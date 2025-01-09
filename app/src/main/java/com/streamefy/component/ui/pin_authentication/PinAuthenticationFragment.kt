package com.streamefy.component.ui.pin_authentication

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doBeforeTextChanged
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.otpview.OTPListener
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.ExitDialog
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.otp.viewmodel.OTPVM
import com.streamefy.component.ui.pin_authentication.dialog.ConfirmPinDialog
import com.streamefy.component.ui.pin_authentication.model.ResetPinRequest
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentPinAuthenticationBinding
import com.streamefy.error.ErrorCodeManager
import com.streamefy.error.ShowError
import com.streamefy.network.MyResource
import com.streamefy.utils.capitalizeFirstLetter
import com.streamefy.utils.gone
import com.streamefy.utils.hideKey
import com.streamefy.utils.imageLoadonLayout
import com.streamefy.utils.invisible
import com.streamefy.utils.loadAny
import com.streamefy.utils.loadPicaso
import com.streamefy.utils.loadUrl
import com.streamefy.utils.remoteKey
import com.streamefy.utils.showMessage
import com.streamefy.utils.visible
import org.koin.androidx.viewmodel.ext.android.viewModel


class PinAuthenticationFragment : BaseFragment<FragmentPinAuthenticationBinding>() {
    override fun netStatus() {}
    override fun bindView(): Int = R.layout.fragment_pin_authentication
    var projectId = "0"
    var phone = ""
    var otp = ""
    var app_background = ""
    var projectName = ""
    private val viewModel: PinVM by viewModel()
    var isPrimaryuser = false
    var isLogin = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isPrimaryuser = SharedPref.getBoolean(PrefConstent.ISPRIMARY_USER)
        arguments?.run {
            projectId = getInt(PrefConstent.PROJECT_ID).toString()
            phone = getString(PrefConstent.PHONE_NUMBER).toString()
            projectName = getString(PrefConstent.PROJECT_NAME).toString()
        }
        isLogin = SharedPref.getBoolean(PrefConstent.ISLOGIN)
        app_background = SharedPref.getString(PrefConstent.AUTH_BACKGROUND).toString()
        binding.ivbackground.loadUrl(app_background)
        otpFieldFocus()
        binding.apply {
            tvResetPin.invisible()
                if (projectName.isNotEmpty()) {
                    textView2.setText(projectName)
                } else {
                    textView2.setText("Welcome")
                }

            ivBack.setOnClickListener {
                    findNavController().navigateUp()
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
            tvProceed.setOnClickListener {

                otp = otpView.text.toString()
                otp.run {
                    otp = this
                    if (otp.isEmpty()) {
                        ShowError.handleError.handleError(ErrorCodeManager.PIN_EMPTY)
                    } else if (otp.length < 4) {
                        ShowError.handleError.handleError(ErrorCodeManager.PIN_LENGTH)
                    } else {

                        viewModel.setPin(requireActivity(), otp, projectId.toInt())
                        observe()
                    }
                }
            }

            otpView.requestFocus()
            otpView.setOtpCompletionListener {
                tvProceed.requestFocus()
                requireActivity().hideKey()
            }

            otpView.cursorColor = ContextCompat.getColor(requireContext(), R.color.black)

            otpView.addTextChangedListener {
                otpView.cursorColor = ContextCompat.getColor(requireContext(), R.color.black)
            }


            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                            findNavController().navigateUp()
                    }
                })

            tvResetPin.setOnClickListener {
                SharedPref.setBoolean(PrefConstent.ISCONFIRM_PIN, true)

                ConfirmPinDialog(requireContext()) {
                    if (it) {
                        viewModel.resetPin(
                            requireContext(),
                            ResetPinRequest(projectId.toInt(), phone)
                        )
                        resetObserve()
                    }
                }.show()
            }

        }
        resetColor()
    }

    private fun resetColor()= with(binding) {
        val isDark=   SharedPref.getBoolean(PrefConstent.IS_DARK)
        if (isDark) {
            constraintLayout.isEnabled=true
            textView2.isEnabled=true
            tvInstruction.isEnabled=true

        } else {
            constraintLayout.isEnabled=false
            tvInstruction.isEnabled=false
            textView2.isEnabled=false
        }
    }


    private fun otpFieldFocus() = with(binding) {

        otpView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                otpView.setItemBackground(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_selected_inputfiled
                    )
                )
            } else {
                otpView.setItemBackground(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.bg_round_stroke_gray
                    )
                )

            }
        }
        tvProceed.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                requireActivity().hideKey()
            }

        }
        tvProceed.remoteKey {
            when (it) {

                StreamEnum.DOWN_DPAD_KEY -> {
                    tvResetPin.requestFocus()
                }

                StreamEnum.UP_DPAD_KEY -> {
                    otpView.requestFocus()
                }

                else -> {}
            }
        }
        tvResetPin.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                tvResetPin.setBackgroundResource(R.drawable.ic_button_selector)
            } else {
                tvResetPin.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        com.otpview.R.color.transparent
                    )
                )
            }
        }
        tvResetPin.remoteKey {
            when (it) {

                StreamEnum.UP_DPAD_KEY -> {
                    tvProceed.requestFocus()
                }

                else -> {}
            }
        }


    }

    private fun observe() {
        viewModel.pinData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
                    showProgress()
                }

                is MyResource.isSuccess -> {
                    SharedPref.setBoolean(PrefConstent.ISRESET_PIN,false)
                    SharedPref.setBoolean(PrefConstent.ISLOGIN, true)
                    SharedPref.setString(PrefConstent.AUTH_PIN, otp)
                    SharedPref.setString(PrefConstent.PROJECT_ID, projectId)
                    if (isAdded) {
                        findNavController().navigate(R.id.homefragment)
                    }
                    dismissProgress()
                }

                is MyResource.isError -> {
                    dismissProgress()
                }

                else -> {}
            }
        }
    }

    private fun resetObserve() {
        viewModel.resetData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
                    showProgress()
                }

                is MyResource.isSuccess -> {
                    dismissProgress()
                    requireContext().showMessage(it.data?.response.toString())
                }

                is MyResource.isError -> {
                    dismissProgress()
                }

                else -> {}
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.otpView.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissProgress()
    }

    override fun onDetach() {
        super.onDetach()
        dismissProgress()
    }
}