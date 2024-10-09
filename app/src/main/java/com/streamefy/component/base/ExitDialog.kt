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
import com.streamefy.utils.remoteKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExitDialog(context: Context) :
    BaseDialog<ExitDialogBinding>(context, R.layout.exit_dialog, R.style.TransparentDialogTheme) {

    override fun setupViews() {
        binding.apply {
            tvExit.setOnClickListener {
                dismiss()
                CoroutineScope(Dispatchers.Main).launch {
                    delay(300)
                    MainActivity().exitApp()
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

            tvContinue.setOnClickListener {
                dismiss()
            }
        }
    }
}