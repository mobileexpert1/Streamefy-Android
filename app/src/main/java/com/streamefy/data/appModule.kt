package com.streamefy.data

import com.streamefy.component.ui.login.LoginViewmodel
import com.streamefy.component.ui.otp.viewmodel.OTPVM
import com.streamefy.component.ui.pin_authentication.PinVM
import com.streamefy.error.ErrorHandler
import com.streamefy.network.ApiService
import com.streamefy.network.RetrofitClient
import com.streamefy.utils.showMessage
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

var appModule= module {

    single {  ErrorHandler(androidContext()) }
    single <ApiService> { RetrofitClient.retrofit(androidContext()).create(ApiService::class.java) }
    viewModel { LoginViewmodel(get()) }
    viewModel { OTPVM(get()) }
    viewModel { PinVM(get()) }

}