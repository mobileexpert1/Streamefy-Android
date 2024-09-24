package com.streamefy.component.ui.home.background

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.SimpleExoPlayer
import com.streamefy.component.ui.home.HomeFragment
import com.streamefy.component.ui.home.model.BackgroundMediaItem
import com.streamefy.databinding.ItemSliderBinding

class BackgroundAdpater(
    private val context: Activity, private val backgroundList: ArrayList<BackgroundMediaItem>, var callBack:(Int)->Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var binding: ItemSliderBinding

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): RecyclerView.ViewHolder {
        binding = ItemSliderBinding.inflate(
                context.layoutInflater,
                viewGroup,
                false)
        return BackgroundHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var data = backgroundList[position]
        Log.e("scnskjnc","sbcjhv $data")
        binding.apply {
            (holder as BackgroundHolder).onBind(
                context,
                data.thumbnailSBucketId,
                data.hlsPlaylistUrl,
                holder.absoluteAdapterPosition
            )
        }
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
    override fun getItemCount() = backgroundList.size

}