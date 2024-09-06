package com.streamefy.component.ui.home.adapter

import androidx.viewbinding.ViewBinding
import com.streamefy.databinding.DrawerItemBinding


import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.streamefy.R
import com.streamefy.component.ui.home.categoryModel.CateModel
import com.streamefy.component.ui.home.model.MediaItem
import com.streamefy.utils.loadUrl


class DrawerAdapter(private val context: Activity, private val mediaList: ArrayList<MediaItem>, var callBack:(Int)->Unit) :
    RecyclerView.Adapter<DrawerAdapter.DrawerView>() {
lateinit var binding: DrawerItemBinding

    override fun onBindViewHolder(viewHolder: DrawerView, position: Int) {
        var data = mediaList[position]
        binding.apply {
            lpVideoProgres.progress=40
            tvSubtitle.text=data.description
            ivCate.loadUrl(data.thumbnailS3bucketId)

            if (position%2==0){
                clParent.setBackgroundColor(ContextCompat.getColor(context,R.color.black))
            }else{
                clParent.setBackgroundColor(ContextCompat.getColor(context,R.color.semi_white))
            }
            viewHolder.itemView.setOnClickListener {
                callBack.invoke(position)
            }

        }



    }
    class DrawerView(itemView: DrawerItemBinding) : ViewHolder(itemView.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawerView {
        binding = DrawerItemBinding.inflate(context.layoutInflater)
        return DrawerView(binding)
    }

    override fun getItemCount(): Int=mediaList.size
}
