package com.streamefy.network

object ServerUrls {
    const val BASE_URL="https://apidev.streamefy.com"
    const val BASE_AUTH_URL="https://authdev.streamefy.com/"
//    const val BASE_AUTH_URL="https://auth.streamefy.com/"
//    const val BASE_URL="http://streamefy-auth-linux-env.eba-9b6qs4fc.ap-south-1.elasticbeanstalk.com/"

    const val LOGIN="api/User/Login"
    const val REFRESH ="api/User/RefreshToken"
    const val OTP ="api/OTP"
    const val OTP_VERIFY ="api/OTP/Verify"
    const val USER_VIDEOS ="api/BunnyVideo/GetUserVideos"
}