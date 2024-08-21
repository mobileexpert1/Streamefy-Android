package com.streamefy.component.ui.login

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.streamefy.network.ApiService

class LoginViewmodel(repo: ApiService) : ViewModel() {//AndroidViewModel(context as Application) {

    val fullNameRegex = Regex("^[a-zA-Z]+\\s+[a-zA-Z]+(\\s+[a-zA-Z]+)?$")

    fun isValidFullName(fullName: String): Boolean {
        return true
    }

    fun isValidPhoneNumberLength(phoneNumber: String): Boolean {
        if (phoneNumber.length < 7) {
            return false
        } else if (phoneNumber.length > 15) {
            return false
        } else {
            return true
        }
    }

}