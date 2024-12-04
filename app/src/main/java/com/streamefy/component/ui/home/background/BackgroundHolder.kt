package com.streamefy.component.ui.home.background

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.Player
import com.streamefy.component.ui.home.HomeFragment.Companion.homeFragment
import com.streamefy.component.ui.video.PlayerHandler
import com.streamefy.databinding.ItemSliderBinding
import com.streamefy.utils.gone
import com.streamefy.utils.loadPicaso
import com.streamefy.utils.loadUrl
import com.streamefy.utils.visible

class BackgroundHolder(var binding: ItemSliderBinding) : RecyclerView.ViewHolder(binding.root) {
    var parent: View
    lateinit var dimen: Pair<Int, Int>
    var oldAdapterPos = -1
    var adapter: BackgroundAdpater? = null
    init {
        parent = itemView
    }

    fun onBind(
       thumbnailSBucketId:String
    ) {
        this.oldAdapterPos = oldAdapterPos
        parent.setTag(this)
        binding.apply {
            if (thumbnailSBucketId.isNotEmpty()) {
               // imageView.visible()
                imageView.loadUrl(thumbnailSBucketId)
            } else {
                imageView.gone()
            }
        }
    }

    fun thumbShow()= with(binding) {
        imageView.run {
            visible()
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(DecelerateInterpolator())
                .setDuration(2000)
                .start()
        }

    }

}
