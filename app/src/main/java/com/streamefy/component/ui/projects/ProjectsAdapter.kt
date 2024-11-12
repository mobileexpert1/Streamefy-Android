package com.streamefy.component.ui.projects

import android.app.Activity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.squareup.picasso.Picasso
import com.streamefy.R
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.home.HomeFragment
import com.streamefy.component.ui.home.model.EventsItem
import com.streamefy.utils.gone
import com.streamefy.utils.visible

class ProjectsAdapter(
    private val context: Activity,
    private val eventList: ArrayList<EventsItem>,
    var callBack: (Int, StreamEnum) -> Unit
) :
    RecyclerView.Adapter<ProjectsAdapter.ProjectView>() {


    override fun onBindViewHolder(viewHolder: ProjectView, position: Int) {
        var data = eventList[position]

        viewHolder.apply {
            itemView.isFocusable = true
            itemView.isClickable = true
//            lpVideoProgres.progress = 40
            tvTitle.text = data.eventTitle



            if (data.media != null) {
                if (data.media?.isNotEmpty()!! && data.media?.size!! >= 1) {
                    Picasso.get().load(data.media!![0].thumbnailS3bucketId).into(thumb)
                    thumb.visible()
                    Log.e("asfafaf", "nkcda ${data.media!![0].thumbnailS3bucketId}")


                    if (data.media!![0].description.isNotEmpty()) {
                        tvSubtitle.text = data.media!![0].description
                        tvSubtitle.visible()
                    } else {
                        tvSubtitle.gone()
                    }

                }
            }
            //  tvMore.visible()


            clEvent.setOnFocusChangeListener { _, hasFocus ->
//                if (hasFocus) {
//                    clEvent.setBackgroundColor(ContextCompat.getColor(context,R.color.red ))
//                } else {
//                    clEvent.setBackgroundColor(ContextCompat.getColor(context,R.color.light_gray))
//                }

                if (hasFocus) {
                    if (viewHolder.absoluteAdapterPosition == eventList.size - 2 && HomeFragment.homeFragment.isEventPagination) {
                        callBack.invoke(viewHolder.absoluteAdapterPosition, StreamEnum.PAGINATION)
                    }
                    HomeFragment.homeFragment.eventFocusPos = viewHolder.absoluteAdapterPosition
                    HomeFragment.homeFragment.focusView = StreamEnum.BOTTOM_EVENT_VIEW
//                    itemView.animate().scaleX(1.03f).scaleY(1.05f).setDuration(200).start()
                    itemView.animate().scaleX(1.1f).scaleY(1.05f).setDuration(200)
                        .withEndAction {
                            HomeFragment.homeFragment.binding.rvCategory.scrollToPosition(
                                absoluteAdapterPosition
                            )
                            itemView.invalidate()
                            itemView.requestLayout()
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
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            callBack.invoke(position, StreamEnum.UP_DPAD_KEY)
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
                callBack.invoke(position, StreamEnum.SINGLE)
            }

        }

    }

    class ProjectView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: AppCompatTextView = itemView.findViewById(R.id.tvTitle)
        val tvSubtitle: AppCompatTextView = itemView.findViewById(R.id.tvSubtitle)
        val tvProjectCount: LinearProgressIndicator = itemView.findViewById(R.id.tvProjectCount)
        val clEvent: ConstraintLayout = itemView.findViewById(R.id.clEvent)
        val thumb: ImageView = itemView.findViewById(R.id.ivThumb)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectView {
        val inflate = context.layoutInflater.inflate(R.layout.projects_item, parent, false)
        return ProjectView(inflate)
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

    fun updateDuration(position: Int, mediaIndex: Int, duraton: Long) {
//        eventList.clear()
        // if (mediaIndex==0) {
        eventList[position].media?.run {
            get(mediaIndex).playbackDuration = duraton.toString()
            //  }
        }
        //eventList[position].media?.get(mediaIndex)?.playbackDuration=duraton.toString()

        notifyItemChanged(position)
    }


    override fun getItemCount(): Int = eventList.size
}