package com.streamefy.utils

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText

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
            Log.e("skcnakjcn","kdcnskldnv $s")
            if (s != null && s.length == 1) {
                nextEditText.requestFocus() // Move focus to the next EditText
            }else if (s != null && s.length==2){
                s.removeRange(0,1)
                nextEditText.requestFocus()
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            Log.e("skcnakjcn","beforeTextChanged $s")
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            Log.e("skcnakjcn","onTextChanged $s")
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
            }else{
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
