package com.streamefy.component.ui.projects

import com.streamefy.component.base.BaseDialog

import android.content.Context
import android.content.DialogInterface
import com.streamefy.MainActivity
import com.streamefy.R
import com.streamefy.component.base.StreamEnum
import com.streamefy.databinding.ExitDialogBinding
import com.streamefy.databinding.LogoutUiBinding
import com.streamefy.databinding.RemoveProjectUiBinding
import com.streamefy.utils.remoteKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

class EventRemoveDialog(context: Context,var callBack:()->Unit) :
    BaseDialog<RemoveProjectUiBinding>(context, R.layout.remove_project_ui, R.style.TransparentDialogTheme) {
    override fun setupViews() {
        binding.apply {
            tvDeleteProject.setOnClickListener {
                dismiss()
                CoroutineScope(Dispatchers.Main).launch {
                    delay(300)
                    callBack.invoke()
                }

            }
            tvCancel.isFocusable = true
            tvCancel.isFocusableInTouchMode = true
            tvCancel.requestLayout()
            tvCancel.requestFocus()

            ivClose.requestFocus()
            tvDeleteProject.remoteKey {
                when(it){
                    StreamEnum.LEFT_DPAD_KEY->{
                        tvCancel.requestFocus()
                    }
                    StreamEnum.RIGHT_DPAD_KEY->{
                        tvCancel.requestFocus()
                    }
                    else->{
                    }
                }
            }
            tvCancel.remoteKey {
                when(it){
                    StreamEnum.RIGHT_DPAD_KEY->{
                        tvDeleteProject.requestFocus()
                    }
                    StreamEnum.LEFT_DPAD_KEY->{
                        tvDeleteProject.requestFocus()
                    }
                    else->{

                    }
                }
            }
            tvCancel.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        super.setOnDismissListener(listener)
        dismiss()
    }
}
