package com.streamefy.component.ui.home.background

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.Player
import com.streamefy.component.ui.home.HomeFragment.Companion.homeFragment
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
   // lateinit var playerHandler:PlayerHandler
    init {
        parent = itemView
    }

    fun onBind(
        context: Activity,thumbnailSBucketId:String,videoUrl:String,position:Int
    ) {
        this.oldAdapterPos = oldAdapterPos
        parent.setTag(this)
        binding.apply {


            if (thumbnailSBucketId.isNotEmpty()) {
                imageView.visible()
                imageView.loadUrl(thumbnailSBucketId)
                videoview.gone()
            } else {
                imageView.gone()

            }

//
//             playerHandler = PlayerHandler(context, videoview)
//          //  playerHandler.pause()
//
//            playerHandler.setMediaUri(videoUrl)
//
//
//
//            playerHandler!!.player?.addListener(object : Player.Listener {
//            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
//                when (playbackState) {
//                    Player.STATE_BUFFERING -> {
//                        Log.e("jnjddc", " STATE_BUFFERING")
//                    }
//
//                    Player.STATE_ENDED -> {
//                        Log.e("jnjddc", " STATE_ENDED")
////                       if (mediaObjects.size>currentVideo){
////                           recyclerview?.scrollToPosition(currentVideo+1)
////                       }
//                        homeFragment.binding.rvBackgVideo.scrollMe()
//                    }
//
//                    Player.STATE_IDLE -> {
//                        Log.e("jnjddc", " STATE_IDLE")
//
//                    }
//
//                    Player.STATE_READY -> {
//                        Log.e("jnjddc", " STATE_READY")
//
////                        if (!isVideoViewAdded) {
////                            try {
////                                addVideoView()
////                                updateDuration()
////                            } catch (e: Exception) {
////
////                            }
////                        }
//
//                    }
//
//                    else -> {
//                    }
//                }
//            }
//
//        })


        }
    }




}
