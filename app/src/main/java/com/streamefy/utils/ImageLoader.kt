package com.streamefy.utils

import android.R.attr.data
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.squareup.picasso.Picasso


fun ImageView.loadUrl(url:String){
    Glide.with(this.context).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).into(this)

}
fun ImageView.loadAny( url:Any){
    Glide.with(this.context).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).into(this)
}

fun ImageView.loadPicaso( url:String){
//    Glide.with(this.context).load(url).into(this)
    Picasso.get().load(url).into(this)
}

fun View.imageLoadonLayout(url:String){
    var view=this
    Glide.with(context)
        .load(url) // the URL of the image
        .into(object : SimpleTarget<Drawable?>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable?>?
            ) {
                view.background=resource
            }
        })
}
