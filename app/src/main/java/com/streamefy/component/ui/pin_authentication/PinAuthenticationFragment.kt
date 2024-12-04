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
    var applogo = ""
    var app_background = ""
    private val viewModel: PinVM by viewModel()
    var isPrimaryuser = false
    var isHome = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isPrimaryuser = SharedPref.getBoolean(PrefConstent.ISPRIMARY_USER)
        arguments?.run {
            projectId = getInt(PrefConstent.PROJECT_ID).toString()
            phone = getString(PrefConstent.PHONE_NUMBER).toString()
        }
        var name = SharedPref.getString(PrefConstent.FULL_NAME).toString()
        // applogo = SharedPref.getString(PrefConstent.APP_LOGO).toString()
        app_background = SharedPref.getString(PrefConstent.AUTH_BACKGROUND).toString()
        // binding.ivApplogo.loadAny(applogo)
        Log.e("sjncsjbc","skncksnc project id $projectId and phone $phone  projectId $projectId" )
        otpFieldFocus()
        binding.apply {
            if (isPrimaryuser) {
                tvResetPin.visible()
            }
//            textView2.setText("Welcome ${capitalizeFirstLetter(name)}! We are thrilled to have you here")
            textView2.setText("Welcome")

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
//                findNavController().navigate(R.id.loginFragment)
                if (isPrimaryuser){
                    findNavController().popBackStack()
                }
                else {
                    ExitDialog(requireContext()).show()
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

                        viewModel.setPin(requireActivity(), otp,projectId.toInt())
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
                tvProceed.requestFocus()
                requireActivity().hideKey()
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
                        if (isPrimaryuser){
                            findNavController().popBackStack()
                        }
                        else {
                           // findNavController().navigate(R.id.loginFragment)
                            ExitDialog(requireActivity()).show()
                        }
                    }
                })

            tvResetPin.setOnClickListener {
                SharedPref.setBoolean(PrefConstent.ISCONFIRM_PIN, true)

                ConfirmPinDialog(requireContext()) {
                    if (it) {
                        /// changes it
//                        findNavController().navigate(R.id.projectfragment)
                        // viewModel.resetPin(requireContext(),"")

                        viewModel.resetPin(requireContext(), ResetPinRequest(projectId.toInt(),phone))
                        resetObserve()
                    }
                }.show()
            }

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
                }

                else -> {}
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
                }

                else -> {}
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
                }

                else -> {}
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
                }

                else -> {}
            }
        }

        otpView.setOnFocusChangeListener { v, hasFocus ->
            Log.e("smskmc", "$hasFocus setOnFocusChangeListener")
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
//                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
//                    tvResetPin.compoundDrawableTintList = ColorStateList.valueOf(
//                        ContextCompat.getColor(
//                            requireActivity(),
//                            R.color.purple
//                        )
//                    )
//                }
//                else {
//                    val drawables = tvResetPin.compoundDrawables
//                    val drawableStart =
//                        drawables[0]  // You can adjust this for top, end, bottom as needed
//                    if (drawableStart != null) {
//                        val wrappedDrawable = DrawableCompat.wrap(drawableStart)
//                        DrawableCompat.setTint(
//                            wrappedDrawable,
//                            ContextCompat.getColor(requireActivity(), R.color.purple)
//                        )
//                        tvResetPin.setCompoundDrawablesWithIntrinsicBounds(
//                            wrappedDrawable,
//                            drawables[1],
//                            drawables[2],
//                            drawables[3]
//                        )
//                    }
//                }
//                tvResetPin.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
            } else {
                tvResetPin.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        com.otpview.R.color.transparent
                    )
                )
//                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
//                    tvResetPin.compoundDrawableTintList = ColorStateList.valueOf(
//                        ContextCompat.getColor(
//                            requireActivity(),
//                            R.color.white
//                        )
//                    )
//
//                }
//                else {
//                    val drawables = tvResetPin.compoundDrawables
//                    val drawableStart =
//                        drawables[0]  // You can adjust this for top, end, bottom as needed
//                    if (drawableStart != null) {
//                        val wrappedDrawable = DrawableCompat.wrap(drawableStart)
//                        DrawableCompat.setTint(
//                            wrappedDrawable,
//                            ContextCompat.getColor(requireActivity(), R.color.white)
//                        )
//                        tvResetPin.setCompoundDrawablesWithIntrinsicBounds(
//                            wrappedDrawable,
//                            drawables[1],
//                            drawables[2],
//                            drawables[3]
//                        )
//                    }
//                }
//                tvResetPin.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
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
                    SharedPref.setBoolean(PrefConstent.ISLOGIN, true)
                    SharedPref.setString(PrefConstent.AUTH_PIN, otp)
                    Log.e("sjxbjsbc", "ksjnckjanc ${it.data}")
                    if (isAdded) {
                        findNavController().navigate(R.id.homefragment)

                    }
                }

                is MyResource.isError -> {
                    dismissProgress()
                }
                else->{}
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
                else->{}
            }
        }
    }
    override fun onResume() {
        super.onResume()
        binding.otpView.setText("")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("skcnmskncm", "skcnsk destroyview")
        dismissProgress()
    }
}