package com.streamefy.component.ui.home.adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.ui.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.squareup.picasso.Picasso
import com.streamefy.R
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.home.HomeFragment
import com.streamefy.component.ui.home.HomeFragment.Companion.homeFragment
import com.streamefy.component.ui.home.categoryModel.CateModel
import com.streamefy.component.ui.home.model.EventsItem
import com.streamefy.utils.convertToMillis
import com.streamefy.utils.gone
import com.streamefy.utils.invisible
import com.streamefy.utils.loadAny
import com.streamefy.utils.loadUrl
import com.streamefy.utils.remoteKey
import com.streamefy.utils.showMessage
import com.streamefy.utils.visible


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
//            lpVideoProgres.progress = 40
            tvTitle.text = data.eventTitle



            if (data.media != null) {
                if (data.media?.isNotEmpty()!! && data.media?.size!! >= 1) {
//                    Picasso.get().load(data.media!![0].thumbnailS3bucketId).into(thumb)
                    thumb.loadUrl(data.media!![0].thumbnailS3bucketId)
                    thumb.visible()
                    Log.e("asfafaf", "nkcda ${data.media!![0].thumbnailS3bucketId}")
                    if (data.media?.size!! == 1) {
                        tvMore.invisible()
                    } else {
                        tvMore.text = (data.media?.size!! - 1).toString() + " More Videos"
                        tvMore.visible()
                    }

                    if (data.media!![0].description.isNotEmpty()) {
                        tvSubtitle.text = data.media!![0].description
                        tvSubtitle.visible()
                    }else{
                        tvSubtitle.gone()
                    }

                } else {
                    tvMore.invisible()
                }
            } else {
                tvMore.invisible()
            }
            //  tvMore.visible()
            data.media?.run {
                if (this.isNotEmpty()) {
                    this[0].run {
                        if (playbackDuration != "0") {
                            lpVideoProgres.visible()
                            var totalDuration = convertToMillis(totalVideoDuration)

                            val duration = playbackDuration.toDouble()
                            //  val currentPosition = playerHandler.getCurrentPosition()
                            val progress = (duration * 100 / totalDuration.toDouble()).toInt()
                            lpVideoProgres.progress = progress
                        } else {
                            lpVideoProgres.gone()
                        }

                    }

                } else {
                    lpVideoProgres.gone()
                }
            }


            clEvent.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    if (viewHolder.absoluteAdapterPosition == eventList.size - 2 && homeFragment.isEventPagination) {
                        callBack.invoke(viewHolder.absoluteAdapterPosition, StreamEnum.PAGINATION)
                    }
                   // HomeFragment.eventVideoIndex = viewHolder.absoluteAdapterPosition
                    homeFragment.focusView = StreamEnum.BOTTOM_EVENT_VIEW
                    itemView.animate().scaleX(1.1f).scaleY(1.05f).setDuration(200)
                        .withEndAction {
//                            itemView.invalidate()
//                            itemView.requestLayout()
//                            homeFragment.binding.rvCategory.scrollToPosition(absoluteAdapterPosition)
                            itemView.post {
                                itemView.requestLayout()
                                homeFragment.binding.rvCategory.scrollToPosition(absoluteAdapterPosition)
                            }

                        }.start()
                    clEvent.setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray))
                } else {
                    itemView.animate().scaleX(1f).scaleY(1f).setDuration(200)
                        .withEndAction {
//                            itemView.invalidate()
//                            itemView.requestLayout()
                        }.start()
                    clEvent.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            com.otpview.R.color.transparent
                        )
                    )
                }


            }

            clEvent.setOnKeyListener { v, keyCode, event ->
//                Log.e("dkvdknv","dnvdkvn all $event")
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            callBack.invoke(position, StreamEnum.UP_DPAD_KEY)
//                            homeFragment.binding.tvPlay.requestFocus()
//                            Log.e("dkvdknv","new changes KEYCODE_DPAD_UP")
                        }
//                    KeyEvent.KEYCODE_DPAD_RIGHT->{
//                        if (viewHolder.absoluteAdapterPosition==eventList.size-2){
//                            callBack.invoke(viewHolder.absoluteAdapterPosition,StreamEnum.PAGINATION)
//                        }
//                    }
                        else -> {
                        }
                    }
                }

                false
            }

            clEvent.setOnClickListener {
                data.media?.run {
                    if (this[0].totalVideoDuration=="00:00:00"){
                        context.showMessage("Something wrong with this Video.Please contact with Admin")
                    }else{
                        callBack.invoke(position, StreamEnum.SINGLE)
                    }
                }

            }

            tvMore.setOnClickListener {
                homeFragment.focusView = StreamEnum.DRAWER_VIEW
                callBack.invoke(position, StreamEnum.MORE)

            }

            tvMore.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                  //  HomeFragment.eventVideoIndex = viewHolder.absoluteAdapterPosition
//                    homeFragment.eventFocusPos = viewHolder.absoluteAdapterPosition
                    homeFragment.focusView = StreamEnum.BOTTOM_EVENT_VIEW
                    // itemView.animate().scaleX(1.03f).scaleY(1f).setDuration(200).start()
                    tvMore.animate().scaleX(1.03f).scaleY(1f).setDuration(200)
                        .withEndAction {
                        itemView.post {
                            itemView.requestLayout()

                            homeFragment.binding.rvCategory.scrollToPosition(absoluteAdapterPosition)
                        }
                    }.start()
                } else {
                    tvMore.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                }
            }

        }

    }

    class CategoriView(itemView: View) : ViewHolder(itemView) {
        val tvTitle: AppCompatTextView = itemView.findViewById(R.id.tvTitle)
        val tvSubtitle: AppCompatTextView = itemView.findViewById(R.id.tvSubtitle)
        val lpVideoProgres: LinearProgressIndicator = itemView.findViewById(R.id.lpVideoProgres)
        val tvMore: AppCompatTextView = itemView.findViewById(R.id.tvMore)
        val clThumb: LinearLayout = itemView.findViewById(R.id.clThumb)
        val clEvent: ConstraintLayout = itemView.findViewById(R.id.clEvent)
        val thumb: ImageView = itemView.findViewById(R.id.ivThumb)
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
//        eventList.clear()
        eventList.addAll(eventList.size - 1, newlist)
//        notifyItemChanged(eventList.size - 1)
        notifyDataSetChanged()
    }

    fun updateDuration(position: Int, mediaIndex: Int?=null, duraton: Long) {
//        eventList.clear()
        // if (mediaIndex==0) {
        if (mediaIndex!=null) {
            eventList[position].media?.run {
                if (this.size>mediaIndex) {
                    get(mediaIndex).playbackDuration = duraton.toString()
                    notifyItemChanged(position)
                }

            }
        }
        //eventList[position].media?.get(mediaIndex)?.playbackDuration=duraton.toString()

    }

fun getList()=eventList
    override fun getItemCount(): Int = eventList.size
}
