package com.streamefy.utils

import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import com.streamefy.component.base.StreamEnum

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun EditText.setupNextFocusOnDigit(nextEditText: EditText) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            Log.e("skcnakjcn", "kdcnskldnv $s")
            if (s != null && s.length == 1) {
                nextEditText.requestFocus() // Move focus to the next EditText
            } else if (s != null && s.length == 2) {
                s.removeRange(0, 1)
                nextEditText.requestFocus()
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            Log.e("skcnakjcn", "beforeTextChanged $s")
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            Log.e("skcnakjcn", "onTextChanged $s")
        }
    })
}


fun EditText.previousFocusOnDigit(nextEditText: EditText) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (s.length < (nextEditText.text?.length ?: 0)) {
                    nextEditText.requestFocus()
                }
            } else {
                nextEditText.requestFocus()
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // No-op
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s.isNullOrEmpty()) {
                // Handle the case where the text is cleared
                nextEditText.requestFocus()
            }
        }
    })
}


fun View.remoteKey(keyBack: (StreamEnum) -> Unit) {
    setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    keyBack.invoke(StreamEnum.DOWN_DPAD_KEY)
                    return@OnKeyListener true
                }

                KeyEvent.KEYCODE_DPAD_UP -> {
                    keyBack.invoke(StreamEnum.UP_DPAD_KEY)
                    return@OnKeyListener true
                }

                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    keyBack.invoke(StreamEnum.LEFT_DPAD_KEY)
                    return@OnKeyListener true
                }

                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    keyBack.invoke(StreamEnum.RIGHT_DPAD_KEY)
                    return@OnKeyListener true
                }
            }
        }
        false
    })
}


fun View.startCountdownTimer(
    duration: Long, // in milliseconds
   //  custm:CountDownTimer,
    onFinish: () -> Unit,
    onTick: (Int) -> Unit
) {
    val countDownTimer = object : CountDownTimer(duration, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val seconds = (millisUntilFinished / 1000).toInt()
            if (seconds == 0) {
                onFinish()
            } else {
                onTick(seconds)
            }
        }

        override fun onFinish() {
            onFinish()
        }
    }
    countDownTimer.start()
   // custm=countDownTimer
}
