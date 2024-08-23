package com.streamefy.utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast

fun Context.showMessage(mesg: String) {
    Toast.makeText(
        this, mesg, Toast.LENGTH_SHORT
    ).show()
}


