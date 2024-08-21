package com.streamefy.component.ui.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.Glide

import com.smarteist.autoimageslider.SliderViewAdapter
import com.streamefy.R
import com.streamefy.utils.gone

class SliderAdapter(private val context: Context, private val images: List<Int>,var callBack:(Int)->Unit) :
    SliderViewAdapter<SliderAdapter.SliderAdapterVH>() {

    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH {
        val inflate = LayoutInflater.from(parent.context).inflate(R.layout.item_slider, null)
        return SliderAdapterVH(inflate)
    }

    override fun onBindViewHolder(viewHolder: SliderAdapterVH, position: Int) {
        var data = images[position]

            viewHolder.tvTitle.gone()
            Glide.with(context)
                .load(data)
                .into(viewHolder.imageView)


        viewHolder.itemView.setOnClickListener { callBack.invoke(position) }
    }

    override fun getCount(): Int {
        return images.size
    }

    class SliderAdapterVH(itemView: View) : ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val tvTitle: AppCompatTextView = itemView.findViewById(R.id.tvTitle)
    }
}
