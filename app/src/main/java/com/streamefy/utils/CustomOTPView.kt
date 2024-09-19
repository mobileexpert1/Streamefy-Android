package com.streamefy.utils

import android.content.Context
import android.content.res.TypedArray
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.streamefy.R

class CustomOTPView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val editTexts: MutableList<EditText> = mutableListOf()
    private val length: Int

    private val otpFieldSize: Float
    private val otpFieldBackground: Int

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomOTPView)
        otpFieldSize = typedArray.getDimension(R.styleable.CustomOTPView_otpFieldSize, 40f)
        otpFieldBackground = typedArray.getResourceId(R.styleable.CustomOTPView_otpFieldBackground, R.drawable.default_background) // default background
        length = typedArray.getInteger(R.styleable.CustomOTPView_otpFieldLength, 4) // Default length
        typedArray.recycle()

        orientation = HORIZONTAL
        createEditTexts()
    }

    private fun createEditTexts() {
        for (i in 0 until length) {
            val editText = LayoutInflater.from(context).inflate(R.layout.otp_edit_text, this, false) as EditText
            editText.layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            editText.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
                for (i in start until end) {
                    if (!Character.isLetterOrDigit(source[i])) {
                        return@InputFilter "" // Reject non-alphanumeric characters
                    }
                }
                null // Accept the input
            })
            editText.setBackgroundResource(otpFieldBackground)
            editText.height = otpFieldSize.toInt() // Set height dynamically
            editText.addTextChangedListener(OtpTextWatcher(editText, this, i))
            editTexts.add(editText)
            addView(editText)
        }
    }

    // TextWatcher for handling input logic
    private inner class OtpTextWatcher(
        private val editText: EditText,
        private val parent: CustomOTPView,
        private val position: Int
    ) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s?.length == 1 && position < length - 1) {
                parent.getChildAt(position + 1).requestFocus()
            } else if (s.isNullOrEmpty() && position > 0) {
                parent.getChildAt(position - 1).requestFocus()
            }
            checkSuccess() // Check if all fields are filled
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    // To retrieve the OTP value
    fun getOTP(): String {
        return editTexts.joinToString("") { it.text.toString() }
    }

    // To clear the input fields
    fun clear() {
        editTexts.forEach { it.setText("") }
        editTexts.first().requestFocus() // Focus the first input
    }

    // Check if all fields are completed
    private fun checkSuccess() {
        if (editTexts.all { it.text.length == 1 }) {
            Toast.makeText(context, "OTP Completed: ${getOTP()}", Toast.LENGTH_SHORT).show()
        }
    }
}
