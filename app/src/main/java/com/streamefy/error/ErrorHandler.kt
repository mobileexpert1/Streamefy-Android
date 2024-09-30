package com.streamefy.error

import android.content.Context
import com.streamefy.utils.showMessage

class ErrorHandler(var context: Context) {
    fun handleError(errorCode: Int) {
        try {
            ErrorCodeManager
            val errorMessage = ErrorCodeManager.getErrorMessage(errorCode)
            context.showMessage(errorMessage)

        } catch (e: Exception) {
            context.showMessage("Error code failed")
        }
    }

    fun message(errorCode: String) {
        try {
            context.showMessage(errorCode)

        } catch (e: Exception) {
            context.showMessage("Error code failed")
        }
    }

    fun handleError(errorCode: Int, action: () -> Unit) {
        val errorMessage = ErrorCodeManager.getErrorMessage(errorCode)
        context.showMessage(errorMessage)
        action()
    }
}