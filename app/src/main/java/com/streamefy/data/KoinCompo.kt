package com.streamefy.data

import com.streamefy.component.ui.login.LoginViewmodel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class KoinCompo:KoinComponent {
    val loginVM: LoginViewmodel by inject()
}