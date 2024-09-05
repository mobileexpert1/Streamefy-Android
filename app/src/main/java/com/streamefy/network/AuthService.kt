package com.streamefy.network

import com.streamefy.component.ui.home.model.HomeResponse
import com.streamefy.component.ui.login.model.LoginRequest
import com.streamefy.component.ui.login.model.LoginResponse
import com.streamefy.component.ui.otp.model.OTPRequest
import com.streamefy.component.ui.otp.model.OTPResponse
import com.streamefy.component.ui.otp.model.VerificationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthService {
    //@Header("authorization": "Basic YWRtaW46cGFzc3dvcmQ=")
    @POST(ServerUrls.LOGIN)
    suspend fun login(
        @Body login: LoginRequest
    ): Response<LoginResponse>


    @POST(ServerUrls.OTP)
    suspend fun otp(
        @Body otp: OTPRequest
    ): Response<OTPResponse>


    @POST(ServerUrls.OTP_VERIFY)
    suspend fun verify(
        @Body otp: VerificationRequest
    ): Response<OTPResponse>

}