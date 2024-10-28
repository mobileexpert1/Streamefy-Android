package com.streamefy.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.streamefy.R
import kotlinx.coroutines.delay
import java.io.IOException

fun Context.showMessage(mesg: String) {
    Toast.makeText(
        this, mesg, Toast.LENGTH_SHORT
    ).show()
}


//fun Context.isNetworkAvailable(): Boolean {
//    try {
//        (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
//            return getNetworkCapabilities(activeNetwork)?.run {
//                when {
//                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
//                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
//                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
//                    else -> false
//                }
//            } ?: false
//        }
//    }catch (e:Exception){
//        return false
//    }
//    catch (e: IOException) {
//        return false
//    }
//}

fun Context.isNetworkAvailable(): Boolean {
    try {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Check API level for network capabilities
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

            return capabilities.run {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            }
        } else {
            // For devices below API 21
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo?.isConnected == true
        }
    } catch (e: Exception) {
        // Optionally log the exception
        return false
    }

    return false
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

fun View.moveDown(context: Context) {
    apply {
        val moveDownAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_down)
        startAnimation(moveDownAnimation)

    }
}

fun View.moveUp(context: Context){
    apply {
        clearAnimation()
        val moveUpAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_up)
        startAnimation(moveUpAnimation)
    }
}

fun View.hideTransition(context: Context){
    apply {
        val moveDownAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_down)
        moveDownAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }
            override fun onAnimationEnd(animation: Animation?) {
                alpha=0f
                visibility = View.GONE
            }
            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        startAnimation(moveDownAnimation)

    }
//    apply {
//
//            animate().cancel()
//               animate().setDuration(0)
//        val parentHeight = (parent as View).height-height.toFloat()
//        alpha = 1f
//        animate()
//            .translationX(0f) // Move to the left (if not already at 0)
//            .translationY(parentHeight) // Move down
//            .alpha(0f) // Fade out
//            .rotationBy(20f)
//            .setInterpolator(DecelerateInterpolator()) // Smooth transition
//            .setDuration(5050) // Duration of the animation
//            .setListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    super.onAnimationEnd(animation)
//                    visibility = View.GONE // Hide the view after the animation
//                    //requestLayout()
//                    Log.e("smcksmc", "skcmks gone $parentHeight")
//                }
//            })
//            .withEndAction {
//                Log.e("smcksmc", "ended true $parentHeight")
//            }
//            .start()
//    }
}
fun View.showTransition(context: Context){
    apply {
//        clearAnimation()
        val moveUpAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_up)
        moveUpAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                visibility = View.VISIBLE
                alpha=1f
            }

            override fun onAnimationEnd(animation: Animation?) {
              //  visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        startAnimation(moveUpAnimation)
    }

//    apply {
//        animate().cancel()
//        val parentHeight = (parent as View).height
//        alpha = 0f
//        translationY = parentHeight.toFloat()
//        visible()
//        animate()
//            .alpha(1f) // Fade in
//            .translationX(0f) // Move to the left (if not already at 0)
//            .translationY(0f) // Move to the original position
//            .setInterpolator(DecelerateInterpolator()) // Smooth transition
//            .setDuration(1000) // Adjust duration as needed
//            .setListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    super.onAnimationEnd(animation)
//                    visibility = View.VISIBLE // Hide the view after the animation
//                    requestLayout()
//                    Log.e("smcksmc", "skcmks visible")
//                }
//            })
//            .start()
//    }
}
