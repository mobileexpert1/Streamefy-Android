package com.streamefy.component.base

import android.app.Dialog
import android.content.Context
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.streamefy.MainActivity
import com.streamefy.R
import com.streamefy.databinding.ExitDialogBinding

class ExitDialog(context: Context) :
    BaseDialog<ExitDialogBinding>(context, R.layout.exit_dialog, R.style.TransparentDialogTheme) {

    override fun setupViews() {
        binding.apply {
            tvExit.setOnClickListener {
                MainActivity().exitApp()
            }
            ivClose.setOnClickListener { dismiss() }
            tvContinue.setOnClickListener {
                dismiss()
            }
        }
    }
}