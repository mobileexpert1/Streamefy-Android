package com.streamefy.utils

import android.util.Log

object LogMessage {
    var log_key="streamfy_"
    fun logeMe(value:String){
        Log.e(log_key,value)
    }
}