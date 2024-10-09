package com.streamefy.component.ui.home
import com.streamefy.component.base.BaseDialog

import android.content.Context
import android.content.DialogInterface
import com.streamefy.MainActivity
import com.streamefy.R
import com.streamefy.component.base.StreamEnum
import com.streamefy.databinding.ExitDialogBinding
import com.streamefy.databinding.LogoutUiBinding
import com.streamefy.utils.remoteKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

class LogoutDialog(context: Context,var callBack:()->Unit) :
    BaseDialog<LogoutUiBinding>(context, R.layout.logout_ui, R.style.TransparentDialogTheme) {

    override fun setupViews() {
        binding.apply {
            tvLogout.setOnClickListener {
                dismiss()
                CoroutineScope(Dispatchers.Main).launch {
                    delay(300)
                    callBack.invoke()
                }

            }
            ivClose.setOnClickListener { dismiss() }
            ivClose.requestFocus()
            ivClose.remoteKey {
                when(it){
                    StreamEnum.DOWN_DPAD_KEY->{
                        tvContinue.requestFocus()
                    }
                    StreamEnum.UP_DPAD_KEY->{
                        tvContinue.requestFocus()
                    }
                    else->{
                    }
                }
            }
            tvLogout.remoteKey {
                when(it){
                    StreamEnum.LEFT_DPAD_KEY->{
                        tvContinue.requestFocus()
                    }
                    StreamEnum.UP_DPAD_KEY->{
                        ivClose.requestFocus()
                    }
                    else->{
                    }
                }
            }
            tvContinue.remoteKey {
                when(it){
                    StreamEnum.RIGHT_DPAD_KEY->{
                        tvLogout.requestFocus()
                    }
                    StreamEnum.UP_DPAD_KEY->{
                        ivClose.requestFocus()
                    }
                    else->{

                    }
                }
            }
            tvContinue.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        super.setOnDismissListener(listener)
        dismiss()
    }
}
