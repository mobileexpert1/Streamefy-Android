package com.streamefy.utils

import android.text.Editable
import android.text.TextWatcher
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
            if (s != null && s.length == 1) {
                nextEditText.requestFocus() // Move focus to the next EditText
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // No-op
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // No-op
        }
    })
}

