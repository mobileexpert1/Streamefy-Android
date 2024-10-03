package com.streamefy.error

import android.content.Context
import android.util.Log
import androidx.compose.ui.res.stringResource
import com.streamefy.R

object ErrorCodeManager {
    const val LOGIN_FAIL = 1
    const val OTP_FAIL = 2
    const val PIN_FAIL = 3
    const val UNKNOWN_ERROR = 4
    const val NETWORK_ISSUE = 5
    const val NOT_FOUND = 6

    const val BLANK=7
    const val NAME_EMPTY=8
    const val NAME_MIN_LENGTH=9
    const val NAME_MAX_LENGTH=10
    const val PASSWORD_EMPTY=11
    const val PASSWORD_MIN_MAX=12

    const val OTP_EMPTY=13
    const val OTP_LENGTH=14


    const val INVALID_CHAR=15
    const val INVALID_PASSWORD_FORMAT=16
    const val PHONE_EMPTY=17
    const val PHONE_LENGTH=18

    const val PIN_LENGTH=19
    const val PIN_EMPTY=20

    private val errorMessages = mapOf(
        LOGIN_FAIL to "Server error, please try again later.",
        OTP_FAIL to "Server error, please try again later.",
        PIN_FAIL to "Database error, please try again later.",
        PIN_FAIL to "Invalid Phone number",
        NOT_FOUND to "Data not found",
        BLANK to "this field should not blank",
        NAME_EMPTY to "Please enter full name",
        NAME_MAX_LENGTH to "More than 25 characters are not allowed",
        NAME_MIN_LENGTH to "Please enter at least 3 characters",
        INVALID_CHAR to  "Oops! invalid characters",
        PASSWORD_EMPTY to   "Please enter password",
        PASSWORD_MIN_MAX to  "Password length should be 8-10 characters",
        INVALID_PASSWORD_FORMAT to  "Invalid password format e.g Test123@",
        OTP_EMPTY to  "Please enter OTP",
        OTP_LENGTH to  "Invalid OTP",
        PHONE_EMPTY to  "Please enter phone number",
        PIN_EMPTY to "Please enter 4-digit PIN",
        PIN_LENGTH to "Invalid PIN",
        PHONE_LENGTH to  "Invalid phone number",
        UNKNOWN_ERROR to "Something went wrong, please try again later.",
        NETWORK_ISSUE to "Please check your internet connection"
    )

    fun getErrorMessage(errorCode: Int): String {
        Log.e("sdksnks","$errorCode")
        return errorMessages[errorCode].toString()
    }

}