package com.streamefy.component.ui.home.adapter

import com.streamefy.databinding.DrawerItemBinding


import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.streamefy.component.ui.home.model.MediaItem
import com.streamefy.utils.convertToMillis
import com.streamefy.utils.gone
import com.streamefy.utils.loadUrl
import com.streamefy.utils.visible


class DrawerAdapter(
    private val context: Activity,
    private val mediaList: ArrayList<MediaItem>,
    var callBack: (Int) -> Unit
) :
    RecyclerView.Adapter<DrawerAdapter.DrawerView>() {
    lateinit var binding: DrawerItemBinding

    override fun onBindViewHolder(viewHolder: DrawerView, position: Int) {
        var data = mediaList[position]
        binding.apply {
            data?.run {
                if (playbackDuration != "0") {
                    if (totalVideoDuration.isNotEmpty()) {
                        var totalDuration = convertToMillis(totalVideoDuration)
                        val duration = playbackDuration.toDouble()
                        val progress = (duration * 100 / totalDuration.toDouble()).toInt()
                        lpVideoProgres.progress = progress

                        var remains = totalDuration - duration
                        var left = getcurrent(remains.toInt().toString())
                        tvDuration.text = "$left"
                        tvDuration.visible()
                    }
                } else {
                    lpVideoProgres.gone()
                    tvDuration.gone()
                }
            }
//            var current = getcurrent(data.playbackDuration)
//            tvDuration.text = "$current of ${data.totalVideoDuration}"

            tvSubtitle.text = data.description
            ivCate.loadUrl(data.thumbnailS3bucketId)

//            if (position%2==0){
//                clParent.setBackgroundColor(ContextCompat.getColor(context,R.color.black))
//            }else{
//                clParent.setBackgroundColor(ContextCompat.getColor(context,R.color.semi_white))
//            }
            viewHolder.itemView.setOnClickListener {
                callBack.invoke(position)
            }

        }


    }

    class DrawerView(itemView: DrawerItemBinding) : ViewHolder(itemView.root) {

    }

    fun getcurrent(playbackduration: String): String {
        val durationMillis = playbackduration.toLong()
        val totalSeconds = durationMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        var total = "0"
        if (hours != 0L) {
            total = "$hours h"
        }
        if (minutes != 0L) {
            total += "$minutes m"
        }
        if (seconds != 0L) {
            total += "$seconds s"
        }

//        if (total !="0"){
//            total = String.format("%02d:%02d:%02d", hours, minutes, seconds)
//        }

        Log.e(
            "sdknjkscn",
            "dkcd $hours minuts $minutes seconds $seconds total $total durations $durationMillis"
        )
        return total

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawerView {
        binding = DrawerItemBinding.inflate(context.layoutInflater)
        return DrawerView(binding)
    }

    fun updateDuration(mediaIndex: Int, duraton: Long) {
//        eventList.clear()
        mediaList[mediaIndex].run {
            playbackDuration = duraton.toString()
        }
        //eventList[position].media?.get(mediaIndex)?.playbackDuration=duraton.toString()

        notifyItemChanged(mediaIndex)
    }

    override fun getItemCount(): Int = mediaList.size
}
