package com.streamefy.utils

import com.streamefy.error.ErrorCodeManager


val namePattern = Regex("^[A-Za-z\\s-]+$")
val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,15}$")

fun nameValidation(name: String) = when {
    name.isBlank() -> {
       // "this field should not blank"
        ErrorCodeManager.BLANK
    }

    name.isEmpty() -> {
        ErrorCodeManager.NAME_EMPTY
    }

    name.length < 3 -> {
        ErrorCodeManager.NAME_MIN_LENGTH
    }

    name.length > 25 -> {
        ErrorCodeManager.NAME_MAX_LENGTH
    }

    !name.matches(namePattern) -> {
        ErrorCodeManager.INVALID_CHAR
    }

    else -> {
        true
    }
}

fun nameAndPassword(name: String, password: String) = when {

   name.isEmpty() -> {
        ErrorCodeManager.NAME_EMPTY
    }

    name.length < 3 -> {
        ErrorCodeManager.NAME_MIN_LENGTH
    }

    name.length > 25 -> {
        ErrorCodeManager.NAME_MAX_LENGTH
    }

//    !name.matches(namePattern) -> {
//        ErrorCodeManager.INVALID_CHAR
//    }

    password.isEmpty() -> {
        ErrorCodeManager.PASSWORD_EMPTY
    }

    password.length< 8 || password.length> 15  -> {
        ErrorCodeManager.PASSWORD_MIN_MAX
    }
    !password.matches(passwordPattern)  -> {
        ErrorCodeManager.INVALID_PASSWORD_FORMAT
    }

    else -> {
        true
    }
}
fun nameWithNumber(name: String, number: String) = when {

   name.isEmpty() -> {
        ErrorCodeManager.NAME_EMPTY
    }

    name.length < 3 -> {
        ErrorCodeManager.NAME_MIN_LENGTH
    }

    name.length > 25 -> {
        ErrorCodeManager.NAME_MAX_LENGTH
    }
    number.isEmpty() -> {
        ErrorCodeManager.PHONE_EMPTY
    }

    number.length< 8 || number.length > 10  -> {
        ErrorCodeManager.PHONE_LENGTH
    }

    else -> {
        true
    }
}

fun passwordValidation(password: String) = when {
    password.isEmpty() -> {
        "Please enter password"
    }

    password.length in 11..7 -> {
        "Password length should be 8-10 characters"
    }

    else -> {
        true
    }
}


