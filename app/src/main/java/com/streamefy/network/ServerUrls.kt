package com.streamefy.network

object ServerUrls {
//    const val BASE_URL="http://20.172.233.97:3000/api/v1/"
    const val BASE_AUTH_URL="http://auth.streamefy.com/"
    const val BASE_URL="http://streamefy-auth-linux-env.eba-9b6qs4fc.ap-south-1.elasticbeanstalk.com/"

    const val LOGIN="api/User/Login"
    const val REFRESH ="api/User/RefreshToken"
    const val OTP ="api/OTP"
    const val OTP_VERIFY ="api/OTP/Verify"
    const val USER_VIDEOS ="api/BunnyVideo/GetUserVideos"
}