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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.hbb20.CountryCodePicker
import com.streamefy.MainActivity
import com.streamefy.R
import com.streamefy.component.base.BaseFragment
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.login.model.LoginRequest
import com.streamefy.country_code.model.CountryCodeModel
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.FragmentLoginBinding
import com.streamefy.network.MyResource
import com.streamefy.utils.LogMessage
import com.streamefy.utils.loadAny
import com.streamefy.utils.phoneNumber
import com.streamefy.utils.remoteKey
import com.streamefy.utils.removeSpacesOnTextChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream


class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    //    var viewmodel = KoinCompo.loginVM
    val viewmodel: LoginViewmodel by viewModel()
    override fun bindView(): Int = R.layout.fragment_login
    var countryCode = 91
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (SharedPref.getString(PrefConstent.COUNTRY_CODE).toString().isNotEmpty()) {
            countryCode = SharedPref.getString(PrefConstent.COUNTRY_CODE).toString().toInt()
        }



        initClickListeners()
//        requireActivity().onBackPressedDispatcher.addCallback {
//            MainActivity().exitApp()
//        }

        binding.etPhoneNumber.requestFocus()


        Log.e("newcode", " code: $countryCode country code")
        binding.ivApplogo.loadAny(R.drawable.ic_logo)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            (requireActivity() as MainActivity).exitApp()
        }

    }

    private fun initClickListeners() = with(binding) {
        tvGetOtp.setOnClickListener {
            //   CountryPicker.loadDataFromXML(requireContext())
            //  var list=    CountryPicker.loadedLibraryMaterList
            //    Log.e("newcode", " code: $countryCode country code $list")

            var validate =
//                nameWithNumber(etFullname.text.toString(), etPhoneNumber.text.toString())
                phoneNumber(etPhoneNumber.text.toString())
            LogMessage.logeMe(validate.toString())
            if (validate) {
                // ShowError.handleError.handleError(validate as Int)
                //   } else {
                SharedPref.setString(PrefConstent.TOKEN, "")
                if (isAdded) {
                    viewmodel.login(
                        requireActivity(),
                        LoginRequest("appsdev096@gmail.com", "Appsdev096#")
//                        LoginRequest("ekamjot-kaur@cssoftsolutions.com", "Admin@123#")
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
        etPhoneNumber.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                Log.e("sjncjsc", "sncjn ${event.action}")
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        tvGetOtp.requestFocus()
                        return@OnKeyListener true
                    }

//                    KeyEvent.KEYCODE_DPAD_UP -> {
//                        etFullname.requestFocus()
//                        return@OnKeyListener true
//                    }

                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        ccCode.requestFocus()
                        return@OnKeyListener true
                    }
                }
            }
            false
        })


        ccCode.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                ccCode.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.semi_transparent
                    )
                )
            } else {
                ccCode.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }
        ccCode.setCountryForPhoneCode(countryCode)
//        ccCode.setCountryForNameCode("US")
        ccCode.registerCarrierNumberEditText(etPhoneNumber);
        ccCode.setOnCountryChangeListener {
            ccCode.registerCarrierNumberEditText(etPhoneNumber);
            var countryCode = ccCode.selectedCountryCode
            var countryCodeName = ccCode.selectedCountryNameCode
            setMaxLength(20)
            Log.e(
                "testtetrttr",
                "${ccCode.isValidFullNumber} countryCodeName.... $countryCodeName countryCode $countryCode"
            )
        }
        ccCode.setPhoneNumberValidityChangeListener(CountryCodePicker.PhoneNumberValidityChangeListener {
            Log.e("testtetrttr", " country validation.... $it ")
            if (it) {
                // var length=etPhoneNumber.text.toString().length + ccCode.selectedCountryCode.length.toInt()
                setMaxLength(ccCode.selectedCountryCode.toInt())
                var formated = ccCode.formattedFullNumber
                var valid = ccCode.fullNumberWithPlus
                Log.e(
                    "testtetrttr",
                    "$valid formated $formated length ${ccCode.selectedCountryCode.toInt()}country number.... $it "
                )
            }


            // your code
        })

        ccCode.isEnabled = true
        ccCode.setCcpClickable(true)
        ccCode.setOnClickListener {
            try {
                lifecycleScope.launch(Dispatchers.Main) {
                    ccCode.launchCountrySelectionDialog()
                }
            } catch (e: Exception) {
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

    fun setMaxLength(length: Int) = with(binding) {
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
            etFullname.setText("")
            etPhoneNumber.setText("")
        }

    }

    val nullObject: String? = null
    fun observe() {
        viewmodel.loginLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is MyResource.isLoading -> {
                    ///loading
                    progressDialog.show()
                }

                is MyResource.isSuccess -> {
                    try {

                        var data = it.data?.response
                        data?.run {
                            SharedPref.setString(PrefConstent.TOKEN, accessToken)
                            SharedPref.setString(PrefConstent.REFRESH_TOKEN, refreshToken)
                            SharedPref.setString(
                                PrefConstent.PHONE_NUMBER,
                                binding.ccCode.formattedFullNumber.toString()
                                //binding.etPhoneNumber.text.toString()
                            )
                            Log.e("slcnslnc", "onResume ${ binding.ccCode.formattedFullNumber.toString()}")
                            SharedPref.setString(
                                PrefConstent.FULL_NAME,
                                "appdev"
//                                binding.etFullname.text.toString()
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
                            binding.ccCode.formattedFullNumber.toString()
                        )
                        if (isAdded) {
                            findNavController().navigate(R.id.otpFragment, bundle)
                        } else {
                            progressDialog.dismiss()
                        }
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                        throw RuntimeException("login getotp")
                    }

                }

                is MyResource.isError -> {
                    progressDialog.dismiss()
                }

                else -> {}
            }
        }
    }

    private fun causeNullPointerCrash() {
        val nullObject: String? = null
        // This will cause a NullPointerException
        try {
            val length = nullObject!!.length

        } catch (e: Exception) {

            logException(e)

        }
    }

    override fun onPause() {
        super.onPause()

        Log.e("skcnmskncm", "skcnsk onpause")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("skcnmskncm", "skcnsk destroyview")
        progressDialog.dismiss()
    }
}