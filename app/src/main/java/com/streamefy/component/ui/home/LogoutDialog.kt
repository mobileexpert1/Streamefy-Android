package com.streamefy.component.ui.home
import com.streamefy.component.base.BaseDialog

import android.content.Context
import com.streamefy.MainActivity
import com.streamefy.R
import com.streamefy.databinding.ExitDialogBinding
import com.streamefy.databinding.LogoutUiBinding

class LogoutDialog(context: Context,var callBack:()->Unit) :
    BaseDialog<LogoutUiBinding>(context, R.layout.logout_ui, R.style.TransparentDialogTheme) {

    override fun setupViews() {
        binding.apply {
            tvLogout.setOnClickListener {
                callBack.invoke()
                dismiss()
            }
            ivClose.setOnClickListener { dismiss() }
            tvContinue.setOnClickListener {
                dismiss()
            }
        }
    }
}