package com.streamefy.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import java.io.IOException

fun Context.showMessage(mesg: String) {
    Toast.makeText(
        this, mesg, Toast.LENGTH_SHORT
    ).show()
}


fun Context.isNetworkAvailable(): Boolean {
    try {
        (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } ?: false
        }
    }catch (e:Exception){
        return false
    }
    catch (e: IOException) {
        return false
    }
}



fun hideSoftKeyboard(activity: Activity, view: View) {
    var gestureDetector =
        GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                try {
                    val imm =
                        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm?.hideSoftInputFromWindow(
                        view.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return true
            }
        })
    view.setOnTouchListener { v, e -> gestureDetector.onTouchEvent(e) }
}

fun View.viewAnimate(){
    this.visibility = View.VISIBLE
    // this.animate().alpha(0.4f).setDuration(5000).startDelay = 1

}
fun View.goneAnimate(){
//    this.visibility = View.GONE
    var image=this
//    this.animate()
//        .alpha(0.4f)
//        .setDuration(50)
//        .startDelay = 1

    this.postDelayed({ image.visibility = View.GONE },500L)


}
fun Activity.hideKey(){
    val inputMethodManager =
        this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    this.currentFocus?.let {
        inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

fun Activity.showKeyboard(view: View) {
//    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)

    view.requestFocus() // Ensure the view is focused
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)

}

fun CountDownTimer.customTimer(duration:Long,countDown:Long){
    val countDownTimer = object : CountDownTimer(duration, countDown) {
        override fun onTick(millisUntilFinished: Long) {
            val seconds = (millisUntilFinished / 1000).toLong()
            onTick(seconds)
        }

        override fun onFinish() {
            onFinish()
        }
    }
    countDownTimer.start()
}


fun convertToMillis(duration: String): Long {
    val parts = duration.split(":")
    val hours = parts[0].toLong()
    val minutes = parts[1].toLong()
    val seconds = parts[2].toLong()

    return (hours * 3600 + minutes * 60 + seconds) * 1000 // Convert to milliseconds
}
