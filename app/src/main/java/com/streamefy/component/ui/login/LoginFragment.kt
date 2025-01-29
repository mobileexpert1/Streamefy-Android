package com.streamefy.component.ui.login

import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.util.Xml
import android.view.KeyEvent
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hbb20.CountryCodePicker
import com.streamefy.BuildConfig
import com.streamefy.MainActivity
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.StreamEnum
import com.streamefy.country_code.model.CountryCodeModel
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentLoginBinding
import com.streamefy.utils.LogMessage
import com.streamefy.utils.hideKey
import com.streamefy.utils.loadAny
import com.streamefy.utils.loadUrl
import com.streamefy.utils.phoneNumber
import com.streamefy.utils.remoteKey
import com.streamefy.utils.removeSpacesOnTextChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream


class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    val viewmodel: LoginViewmodel by viewModel()
    override fun bindView(): Int = R.layout.fragment_login
    var countryCode = 91
    var realnumer = ""
    var admin_email = ""
    var admin_password = ""
    var isDark = false
    var logo = ""
    var background = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (SharedPref.getString(PrefConstent.COUNTRY_CODE).toString().isNotEmpty()) {
            countryCode = SharedPref.getString(PrefConstent.COUNTRY_CODE).toString().toInt()
        }
        realnumer = SharedPref.getString(PrefConstent.REALNUMBER).toString()
        isDark = SharedPref.getBoolean(PrefConstent.IS_DARK)
        logo = SharedPref.getString(PrefConstent.APP_LOGO).toString()
        background = SharedPref.getString(PrefConstent.AUTH_BACKGROUND).toString()
        admin_email = BuildConfig.Admin_email
        admin_password = BuildConfig.Password
        resetColor()

        initClickListeners()
        binding.etPhoneNumber.requestFocus()

        binding.ivApplogo.loadAny(R.drawable.ic_logo_ori)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            MainActivity().exitApp()
        }

    }

    private fun resetColor() = with(binding) {
//        handle themes
        if (isDark) {
            constraintLayout.isEnabled = true
        } else {
            constraintLayout.isEnabled = false
        }

        if (logo.isNotEmpty()) {
            ivApplogo.loadUrl(logo)
        }
        ivbackground.loadUrl(background)
    }

    private fun initClickListeners() = with(binding) {
        tvGetOtp.setOnClickListener {
            val validate = phoneNumber(etPhoneNumber.text.toString())
            LogMessage.logeMe(validate.toString())
            if (validate) {
                if (isAdded) {
                    val formated_Number = ccCode.formattedFullNumber.toString()
                    var updated_number = formated_Number

                    lifecycleScope.launch {
                        if (ccCode.selectedCountryName == "India") {
                            updated_number = replaceSpaceFromLastIfMoreThanTwo(formated_Number)
                        }
                        withContext(Dispatchers.Main) {
                            SharedPref.setString(
                                PrefConstent.PHONE_NUMBER,
                                updated_number
                            )
                            SharedPref.setString(
                                PrefConstent.REALNUMBER,
                                binding.etPhoneNumber.text.toString()
                            )
                            SharedPref.setString(PrefConstent.FULL_NAME, "appdev")
                            SharedPref.setString(
                                PrefConstent.COUNTRY_CODE,
                                binding.ccCode.selectedCountryCode
                            )
                            val bundle = Bundle()
                            bundle.putString(
                                PrefConstent.PHONE_NUMBER,
                                updated_number
                            )
                            if (isAdded) {
                                findNavController().navigate(R.id.otpFragment, bundle)
                            }
                        }
                    }
                } else {
                    onAttach(requireActivity())
                }
            }

        }
        etPhoneNumber.setOnEditorActionListener { v, actionId, event ->
            Log.e("numbers", "${ccCode.selectedCountryNameCode} setOnEditorActionListener ${etPhoneNumber.text}")
            lifecycleScope.launch(Dispatchers.Main) {
                delay(100)
                if (etPhoneNumber.text.isNotEmpty()) {
                    etPhoneNumber.setSelection(etPhoneNumber.text.length)
                    tvGetOtp.requestFocus()
                    formatPhoneNumber(etPhoneNumber.text.toString(),ccCode.selectedCountryNameCode)
                } else {
                    etPhoneNumber.setSelection(etPhoneNumber.text.length)
                }

            }
            false
        }
        etPhoneNumber.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                // if (etPhoneNumber.text.isNotEmpty()) {
//                etPhoneNumber.setSelection(etPhoneNumber.text.length)
//                etPhoneNumber.setBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.semi_transparent
//                    )
//                )
            } else {
//                etPhoneNumber.setBackgroundColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.white
//                    )
//                )
            }
        }
//        handle key movement
        etPhoneNumber.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        tvGetOtp.requestFocus()
                        return@OnKeyListener true
                    }

                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        ccCode.requestFocus()
                        return@OnKeyListener true
                    }
                }
            }
            false
        })
//        change background color of country code picker view
        ccCode.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
//        handle focus of country code picker view
        ccCode.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                ccCode.setBackgroundResource(R.drawable.ic_country_code_selected_bg)
            } else {
                ccCode.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        android.R.color.transparent
                    )
                )
            }
        }
//        set country code
        ccCode.setCountryForPhoneCode(countryCode)
//        bind countr code view with phone number input field
        ccCode.registerCarrierNumberEditText(etPhoneNumber);
        setMaxLength(20)
        ccCode.setOnCountryChangeListener {
            Log.e("numbers", "onchange ${ccCode.formattedFullNumber} length ")
            etPhoneNumber.setText("")
            ccCode.registerCarrierNumberEditText(etPhoneNumber);
            // setMaxLength(20)
        }
        ccCode.setPhoneNumberValidityChangeListener({
            var length = ccCode.fullNumber.length
            Log.e(
                "numbers",
                "$it setPhoneNumberValidityChangeListener ${ccCode.selectedCountryCode.toInt()} length ${ccCode.fullNumber.length} sub length $length"
            )
            if (it) {
                length = length + ccCode.selectedCountryCode.toString().replace("+", "").toInt()
                setMaxLength(length)
            }
        })

        ccCode.isEnabled = true
        ccCode.setCcpClickable(true)
        ccCode.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                try {
//                    launch country code dialog
                    ccCode.launchCountrySelectionDialog()
                } catch (e: Exception) {
                }
            }
        }


        tvGetOtp.remoteKey {
            when (it) {
                StreamEnum.UP_DPAD_KEY -> {
                    etPhoneNumber.requestFocus()
                }

                else -> {}
            }
        }

//        not in used now

        etFullname.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                if (etFullname.text.isNotEmpty()) {
                    etFullname.setSelection(etFullname.text.length)
                }
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
        etFullname.removeSpacesOnTextChange()
        etFullname.setOnEditorActionListener { v, actionId, event ->
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


    }

    fun formatPhoneNumber(number: String, countryCode: String): String {
        val phoneNumberUtil = PhoneNumberUtil.getInstance()

        try {
            // Parse the phone number with country code
            val parsedNumber = phoneNumberUtil.parse(countryCode + number, countryCode)
            val results = phoneNumberUtil.format(
                parsedNumber,
                PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
          var number=  phoneNumberUtil.getExampleNumber(countryCode).nationalNumber
            Log.e("formatednum", "results ${results} number $number")
            return results
        } catch (e: Exception) {
            Log.e("formatednum", "hello ${e.message}")
            return "Invalid number"
        }
    }

    private fun setMaxLength(length: Int) = with(binding) {
        val filterArray = arrayOf<InputFilter>(InputFilter.LengthFilter(length))
        etPhoneNumber.filters = filterArray
    }

    fun setMinMaxLength(minLength: Int, maxLength: Int) = with(binding) {
        val maxLengthFilter = InputFilter.LengthFilter(maxLength)
        val minLengthFilter = InputFilter { source, start, end, dest, dstart, dend ->
            val newText = dest.toString().substring(0, dstart) + source.toString() + dest.toString()
                .substring(dend)
            if (newText.length < minLength) {
                return@InputFilter ""
            }
            null
        }

        etPhoneNumber.filters = arrayOf(minLengthFilter, maxLengthFilter)
    }

    private fun readCountriesFromXml(): List<CountryCodeModel> {
        val countries = mutableListOf<CountryCodeModel>()
        val parser: XmlPullParser = Xml.newPullParser()

        try {
            // Open the raw resource
            val inputStream: InputStream = resources.openRawResource(R.raw.code_template)

            // Set the input stream to the parser
            parser.setInput(inputStream, null)

            var eventType = parser.eventType
            var currentCountry: CountryCodeModel? = null

            // Parse the XML
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val tagName = parser.name
                        when (tagName) {
                            "country" -> {
                                // Initialize a new Country object when we start reading a country
                                currentCountry = CountryCodeModel()
                            }

                            "name" -> {
                                // Read country name
                                currentCountry?.name = parser.nextText()
                            }

                            "english_name" -> {
                                // Read english name
                                currentCountry?.englishName = parser.nextText()
                            }

                            "name_code" -> {
                                // Read name code
                                currentCountry?.nameCode = parser.nextText()
                            }

                            "phone_code" -> {
                                // Read phone code
                                currentCountry?.phoneCode = parser.nextText()
                            }

                            "dialing_length" -> {
                                // Read dialing length
                                currentCountry?.dialingLength = parser.nextText()
                            }

                            "flag" -> {
                                // Read flag (this would typically be a drawable resource reference)
                                currentCountry?.flagResID = parser.nextText()
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        // When we finish reading a country, add it to the list
                        if (parser.name == "country" && currentCountry != null) {
                            countries.add(currentCountry)
                            currentCountry = null
                        }
                    }
                }
                eventType = parser.next()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("slcnslnc", "onResume $e")
        }

        return countries
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            ccCode.invalidate()
            etFullname.setText("")
            if (realnumer != null) {
                if (realnumer.toString().isNotEmpty()) {
                    etPhoneNumber.setText(realnumer)
                }
            }
            ccCode.setCountryForPhoneCode(countryCode)
        }

    }


    //    add dash for indian dialer code
    private fun replaceSpaceFromLastIfMoreThanTwo(str: String): String {
        val spaceCount = str.count { it == ' ' }
        if (spaceCount >= 2) {
            val lastSpaceIndex = str.lastIndexOf(' ')
            return str.substring(0, lastSpaceIndex) + "-" + str.substring(lastSpaceIndex + 1)
        }
        return str
    }


    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressDialog.dismiss()
    }

    override fun netStatus() {
    }
}