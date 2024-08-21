package com.streamefy.component.ui.login

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel

class LoginViewmodel(context: Context): AndroidViewModel(context as Application) {

    val fullNameRegex = Regex("^[a-zA-Z]+\\s+[a-zA-Z]+(\\s+[a-zA-Z]+)?$")

    fun isValidFullName(fullName: String): Boolean {
        return fullNameRegex.matches(fullName)
    }

    fun isValidPhoneNumberLength(phoneNumber: String): Boolean {
        val minLength = 7 // Adjust as needed
        val maxLength = 15 // Adjust as needed
        return phoneNumber.length in minLength..maxLength
    }

}