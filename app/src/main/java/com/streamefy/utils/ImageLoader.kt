package com.streamefy.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso

fun ImageView.loadUrl( url:String){
    Glide.with(this.context).load(url).into(this)

}
fun ImageView.loadAny( url:Any){
    Glide.with(this.context).load(url).into(this)
}

fun ImageView.loadPicaso( url:String){
//    Glide.with(this.context).load(url).into(this)
    Picasso.get().load(url).into(this)
}