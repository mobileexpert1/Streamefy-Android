package com.streamefy.component.ui.pin_authentication.dialog

import android.content.Context
import com.streamefy.MainActivity
import com.streamefy.R
import com.streamefy.component.base.BaseDialog
import com.streamefy.component.base.StreamEnum
import com.streamefy.data.PrefConstent
import com.streamefy.data.SharedPref
import com.streamefy.databinding.ConfirmPinDialogBinding
import com.streamefy.databinding.ExitDialogBinding
import com.streamefy.utils.remoteKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConfirmPinDialog(context: Context, var isReset: Boolean, var callBack: (Boolean) -> Unit) :
    BaseDialog<ConfirmPinDialogBinding>(
        context,
        R.layout.confirm_pin_dialog,
        R.style.TransparentDialogTheme
    ) {
    var isConfirm = false
    override fun setupViews() {
        binding.apply {
            if (isReset) {
                // reset pin
                var phone = SharedPref.getString(PrefConstent.PHONE_NUMBER).toString()
                var value =
                    "New PIN will be sent to your \\nand Mobile Number ${maskPhoneNumber(phone)}"

                tvMessage.setText(value)
                ivClose.setImageResource(R.drawable.ic_reset_pin)
            } else {
                var project = SharedPref.getString(PrefConstent.PROJECT_NAME).toString()
                var value =
                    "Are you sure you want to rest the PIN for the event “$project"
                tvMessage.setText(value)
                ivClose.setImageResource(R.drawable.ic_question_marks)
            }

            setOnDismissListener {
                callBack.invoke(isConfirm)
            }
            ivClose.setOnClickListener {
                isConfirm = false
                dismiss()
            }
            ivClose.requestFocus()
            ivClose.remoteKey {
                when (it) {
                    StreamEnum.LEFT_DPAD_KEY -> {
                        tvContinue.requestFocus()
                    }

                    else -> {
                    }
                }
            }

            tvContinue.remoteKey {
                when (it) {
                    StreamEnum.RIGHT_DPAD_KEY -> {
                        ivClose.requestFocus()
                    }

                    else -> {
                    }
                }
            }

            tvContinue.setOnClickListener {
                isConfirm = true
                dismiss()
            }
        }
    }


    fun maskPhoneNumber(phoneNumber: String): String {
        val visiblePartLength = 2  // Number of digits to show at the end
        val maskedPartLength = phoneNumber.length - visiblePartLength
        val maskedPart = "X".repeat(maskedPartLength)
        val visiblePart = phoneNumber.takeLast(visiblePartLength)

        return "$maskedPart$visiblePart"
    }
}