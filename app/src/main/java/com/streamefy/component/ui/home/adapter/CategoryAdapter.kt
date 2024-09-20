package com.streamefy.component.ui.home.adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.streamefy.R
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.home.categoryModel.CateModel
import com.streamefy.component.ui.home.model.EventsItem
import com.streamefy.utils.gone
import com.streamefy.utils.invisible
import com.streamefy.utils.loadAny
import com.streamefy.utils.loadUrl


class CategoryAdapter(
    private val context: Activity,
    private val eventList: ArrayList<EventsItem>,
    var callBack: (Int, StreamEnum) -> Unit
) :
    RecyclerView.Adapter<CategoryAdapter.CategoriView>() {


    override fun onBindViewHolder(viewHolder: CategoriView, position: Int) {
        var data = eventList[position]

        viewHolder.apply {
            itemView.isFocusable = true
            itemView.isClickable = true
            lpVideoProgres.progress = 40
            tvSubtitle.text = data.eventTitle
            tvTitle.text = data.eventTitle
//            imageView.loadAny(data.image)

            if (data.media != null) {
                if (data.media?.isNotEmpty()!!) {
                    tvSubtitle.text = data.media!![0].description
                    tvMore.text = data.media?.size.toString() + " more video"
                    // imageView.loadUrl(data.media!![0].thumbnailS3bucketId)
                    Glide.with(context)
                        .load(R.drawable.ic_video_play)
                        .into(imageView)
                    Log.e("asfafaf", "nkcda ${data.media!![0].thumbnailS3bucketId}")
                } else {
                    tvMore.invisible()
                }
            } else {
                tvMore.invisible()
            }

            viewHolder.itemView.setOnClickListener {
                callBack.invoke(position,StreamEnum.SINGLE)
            }

//            tvMore.setOnClickListener {
//               // callBack.invoke(position,StreamEnum.MORE)
//            }

        }

    }
    class CategoriView(itemView: View) : ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivCate)
        val tvTitle: AppCompatTextView = itemView.findViewById(R.id.tvTitle)
        val tvSubtitle: AppCompatTextView = itemView.findViewById(R.id.tvSubtitle)
        val lpVideoProgres: LinearProgressIndicator = itemView.findViewById(R.id.lpVideoProgres)
        val tvMore: AppCompatTextView = itemView.findViewById(R.id.tvMore)
        val clThumb: LinearLayout = itemView.findViewById(R.id.clThumb)
        val clEvent: ConstraintLayout = itemView.findViewById(R.id.clEvent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriView {
        val inflate = context.layoutInflater.inflate(R.layout.home_category_item, parent, false)
        return CategoriView(inflate)
    }

    fun update(newlist: ArrayList<EventsItem>) {
       // eventList.clear()
        eventList.addAll(newlist)
        notifyDataSetChanged()
    }

    fun pagination(newlist: ArrayList<EventsItem>) {
        eventList.addAll(newlist)
        notifyItemChanged(eventList.size - 1)
    }

    override fun getItemCount(): Int = eventList.size
}
