package com.streamefy.utils

import android.content.Context
import android.widget.Toast

fun Context.showMessage(mesg: String) {
    Toast.makeText(
        this, mesg, Toast.LENGTH_SHORT
    ).show()
}