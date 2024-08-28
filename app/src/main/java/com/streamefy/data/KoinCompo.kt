package com.streamefy.data

import com.streamefy.component.ui.login.LoginViewmodel
import com.streamefy.component.ui.otp.viewmodel.OTPVM
import com.streamefy.component.ui.pin_authentication.PinVM
import com.streamefy.error.ErrorHandler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object KoinCompo:KoinComponent {
    val handleError: ErrorHandler by inject()
    val loginVM: LoginViewmodel by inject()
    val otpVm: OTPVM by inject()
    val pinVm: PinVM by inject()
}