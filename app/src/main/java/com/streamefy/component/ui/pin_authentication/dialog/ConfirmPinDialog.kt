package com.streamefy.component.ui.pin_authentication.dialog

import android.content.Context
import android.util.Log
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

class ConfirmPinDialog(context: Context, var callBack: (Boolean) -> Unit) :
    BaseDialog<ConfirmPinDialogBinding>(
        context,
        R.layout.confirm_pin_dialog,
        R.style.TransparentDialogTheme
    ) {
    var isConfirm = false
    var isReset = false
    override fun setupViews() {
        isReset= SharedPref.getBoolean(PrefConstent.ISCONFIRM_PIN)
        binding.apply {

            if (isReset) {
                // reset pin
                var phone = SharedPref.getString(PrefConstent.PHONE_NUMBER).toString()
                    .replace(" ","")
                    .replace("-","")
                    .replace("+","")
                    .drop(2)
                Log.e("sjbdsjbc","$phone smxks $isReset")
                var value =""

               var email= SharedPref.getString(PrefConstent.USER_EMAIL)
                if (email!=null){
                    value = "New PIN will be sent to your Email (${maskEmail(email)}) \nand Mobile Number (${maskPhoneNumber(phone)})"
                }else{
                    value = "New PIN will be sent to your Mobile Number ${maskPhoneNumber(phone)}"
                }

                tvMessage.setText(value)
                ivClose.setImageResource(R.drawable.ic_confirm_pin)
            } else {
                var project = SharedPref.getString(PrefConstent.PROJECT_NAME).toString()
                var value =
                    "Are you sure you want to reset the PIN for the event ($project)"
                tvMessage.setText(value)
                ivClose.setImageResource(R.drawable.ic_question_marks)
            }

            setOnDismissListener {
                callBack.invoke(isConfirm)
            }
            tvCancel.setOnClickListener {
                isConfirm = false
                dismiss()
            }
            tvCancel.requestFocus()
            tvCancel.remoteKey {
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
                        tvCancel.requestFocus()
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


    fun maskEmail(email: String): String {

        val visiblePartLength = email.length - 4
        val maskedPartLength = 4
        val maskedPart = "*".repeat(maskedPartLength)
        val visiblePart = email.take(visiblePartLength)

        return "$maskedPart$visiblePart"
    }

}