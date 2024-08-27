package com.streamefy.error

import android.content.Context
import androidx.compose.ui.res.stringResource
import com.streamefy.R

object ErrorCodeManager {
    const val LOGIN_FAIL = 1
    const val OTP_FAIL = 2
    const val PIN_FAIL = 3
    const val UNKNOWN_ERROR = 4


    private val errorMessages = mapOf(
        LOGIN_FAIL to "Server error, please try again later.",
        OTP_FAIL to "Server error, please try again later.",
        PIN_FAIL to "Database error, please try again later.",
        PIN_FAIL to "Invalid Phone number!",
        UNKNOWN_ERROR to "Unknown error, please try again later."
    )

    fun getErrorMessage(errorCode: Int): String {
        return errorMessages[errorCode] ?: "Unknown error, please try again later."
    }

}