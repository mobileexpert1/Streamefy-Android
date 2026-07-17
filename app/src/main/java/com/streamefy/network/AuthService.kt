package com.streamefy.network

import com.streamefy.component.ui.login.model.LoginRequest
import com.streamefy.component.ui.login.model.LoginResponse
import com.streamefy.component.ui.otp.model.OTPRequest
import com.streamefy.component.ui.otp.model.OTPResponse
import com.streamefy.component.ui.otp.model.VerificationRequest
import com.streamefy.component.ui.otp.model.VerifyResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST(ServerUrls.LOGIN)
    suspend fun login(
        @Body login: LoginRequest
    ): Response<LoginResponse>

    @POST(ServerUrls.OTP)
    suspend fun otp(
        @Body request: OTPRequest
    ): Response<OTPResponse>

    @POST(ServerUrls.OTP_VERIFY)
    suspend fun verify(
        @Body otp: VerificationRequest
    ): Response<VerifyResponse>

}