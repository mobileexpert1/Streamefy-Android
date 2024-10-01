package com.streamefy.utils

fun capitalizeFirstLetter(fieldName: String): String {
    return if (fieldName.isNotEmpty()) {
        fieldName[0].uppercaseChar() + fieldName.substring(1)
    } else {
        fieldName // Return as is if empty
    }
}