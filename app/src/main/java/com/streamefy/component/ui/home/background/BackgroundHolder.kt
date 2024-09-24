package com.streamefy.component.ui.home.background

import android.app.Activity
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.streamefy.component.ui.video.PlayerHandler
import com.streamefy.databinding.ItemSliderBinding
import com.streamefy.utils.gone
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
        context: Activity,thumbnailSBucketId:String,videoUrl:String,position:Int
    ) {
        this.oldAdapterPos = oldAdapterPos
        parent.setTag(this)
        binding.apply {
            var playerHandler = PlayerHandler(context, videoview)
            playerHandler.setMediaUri(videoUrl)
//            if (thumbnailSBucketId.isNotEmpty()) {
//                imageView.visible()
//                imageView.loadUrl(thumbnailSBucketId)
//                videoview.gone()
//            } else {
//                imageView.gone()
//
//            }
        }
    }

}
