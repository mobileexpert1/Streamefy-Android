package com.streamefy.component.ui.networkui

import android.content.Context
import androidx.lifecycle.lifecycleScope
import com.streamefy.MainActivity
import com.streamefy.R
import com.streamefy.component.base.BaseDialog
import com.streamefy.component.base.StreamEnum
import com.streamefy.databinding.ExitDialogBinding
import com.streamefy.databinding.NetworkUiBinding
import com.streamefy.utils.gone
import com.streamefy.utils.remoteKey
import com.streamefy.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.view.WindowManager.LayoutParams
import com.streamefy.component.base.BaseFragment

class NetDialog(context: Context) :
    BaseDialog<NetworkUiBinding>(context, R.layout.network_ui, R.style.fullDialogTheme) {

    override fun setupViews() {
        window?.let {
            val params = it.attributes
            params.width = LayoutParams.MATCH_PARENT
            params.height = LayoutParams.MATCH_PARENT
            it.attributes = params
        }
        setCancelable(false)
        binding.apply {
            tvExit.setOnClickListener {
                dismiss()
                CoroutineScope(Dispatchers.Main).launch {
                    delay(100)
                    MainActivity().exitApp()
                }
            }
            tvTry.isFocusable = true
            tvTry.isFocusableInTouchMode = true
            tvTry.requestLayout()
            tvTry.requestFocus()

            tvExit.remoteKey {
                when (it) {
                    StreamEnum.RIGHT_DPAD_KEY -> {
                        tvExit.requestFocus()
                    }

                    StreamEnum.LEFT_DPAD_KEY -> {
                        tvTry.requestFocus()
                    }

                    else -> {

                    }
                }
            }

            tvTry.remoteKey {
                when (it) {
                    StreamEnum.RIGHT_DPAD_KEY -> {
                        tvExit.requestFocus()
                    }

                    StreamEnum.LEFT_DPAD_KEY -> {
                        tvTry.requestFocus()
                    }

                    else -> {

                    }
                }
            }

            tvTry.setOnClickListener {
                BaseFragment.progressDialog.show()
                tvTry.gone()
                tvExit.gone()
                CoroutineScope(Dispatchers.IO).launch {
                    delay(5000)
                    withContext(Dispatchers.Main) {
                        BaseFragment.progressDialog.dismiss()
                        tvExit.visible()
                        tvTry.apply {
                            visible()
                            isFocusable = true
                            isFocusableInTouchMode = true
                            requestLayout()
                            requestFocus()
                        }
                        if (BaseFragment.isNetworkAvailable){dismiss()}
                    }

                }
            }
        }
    }

}
