package com.streamefy.component.ui.home
import com.streamefy.component.base.BaseDialog

import android.content.Context
import android.content.DialogInterface
import com.streamefy.MainActivity
import com.streamefy.R
import com.streamefy.databinding.ExitDialogBinding
import com.streamefy.databinding.LogoutUiBinding
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
