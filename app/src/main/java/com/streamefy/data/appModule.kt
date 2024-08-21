package com.streamefy.data

import com.streamefy.component.ui.login.LoginViewmodel
import com.streamefy.network.ApiService
import com.streamefy.network.RetrofitClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

var appModule= module {

    single <ApiService> { RetrofitClient.retrofit(androidContext()).create(ApiService::class.java) }

    viewModel { LoginViewmodel(get()) }
}