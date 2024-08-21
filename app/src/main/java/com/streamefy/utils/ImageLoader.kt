package com.streamefy.utils

import android.widget.ImageView
import com.bumptech.glide.Glide

fun ImageView.loadUrl( url:String){
    Glide.with(this).load(url)

}
fun ImageView.loadAny( url:Any){
    Glide.with(this).load(url)
}