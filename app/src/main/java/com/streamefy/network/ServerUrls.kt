package com.streamefy.network

object ServerUrls {

// production server
    const val BASE_URL = "https://api.streamefy.com/"
    const val BASE_AUTH_URL = "https://auth.streamefy.com"

// dev server
   // const val BASE_URL = "https://apidev.streamefy.com/"
   // const val BASE_AUTH_URL = "https://authdev.streamefy.com"

    const val LOGIN = "api/User/Login"
    const val OTP = "api/OTP"
    const val OTP_VERIFY = "api/OTP/Verify"

    const val RESETPIN = "api/Project/PrimaryUser/ResetPin"
    const val GET_PROJECTS = "api/Project/PrimaryUser/Projects"
    const val SINGLE_VIDEO = "api/StreamefyMedia"

    // new end points
    const val USER_VIDEOS = "api/OTT/GetUserVideos"
    const val PLAY_BACK = "api/StreamefyMedia/Playback"
    const val PIN_VERIFICATION = "api/OTT/Authorize/"
    const val REMOVE_PROJECT = "api/Project/PrimaryUser/Remove"

}