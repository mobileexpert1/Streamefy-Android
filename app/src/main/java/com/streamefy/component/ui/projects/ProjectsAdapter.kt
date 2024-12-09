package com.streamefy.component.ui.projects

import android.app.Activity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.squareup.picasso.Picasso
import com.streamefy.R
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.home.HomeFragment
import com.streamefy.component.ui.home.model.EventsItem
import com.streamefy.component.ui.projects.EventFragment.Companion.eventFragment
import com.streamefy.component.ui.projects.EventFragment.Companion.focusedIndex
import com.streamefy.component.ui.projects.model.ResponseItem
import com.streamefy.utils.gone
import com.streamefy.utils.loadAny
import com.streamefy.utils.loadUrl
import com.streamefy.utils.remoteKey
import com.streamefy.utils.visible
import java.text.SimpleDateFormat
import java.util.Locale

class ProjectsAdapter(
    private val context: Activity,
    private val eventList: ArrayList<ResponseItem>,
    var callBack: (Int, StreamEnum) -> Unit
) :
    RecyclerView.Adapter<ProjectsAdapter.ProjectView>() {


    override fun onBindViewHolder(viewHolder: ProjectView, position: Int) {
        var data = eventList[position]

        viewHolder.apply {
            itemView.isFocusable = true
            itemView.isClickable = true
            Log.e("membercheck", "newlist ${data.isLast}")
            if (data.isLast){
                thumb.setBackgroundColor(ContextCompat.getColor(context,R.color.black))
                thumb.loadAny(R.drawable.ic_add_event)
                tvAddEvent.visible()
                tvTitle.gone()
                tvProjectCount.gone()
                tvSubtitle.gone()
                mview.gone()
                clEvent.setOnClickListener {
                    callBack.invoke(position, StreamEnum.LAST_EVENT)
                }
            }else {
                tvTitle.text = data.name
//            tvSubtitle.text = data.createDate
                tvProjectCount.text = data.mediaCount.toString()
                var date = updateDate(data.createDate)
                tvSubtitle.text = date

                if (data.thumbnail != null) {
                    // Picasso.get().load(data.thumbnail).into(thumb)
                    thumb.loadUrl(data.thumbnail)
                }
                clEvent.setOnClickListener {
                    callBack.invoke(position, StreamEnum.SINGLE)
                }
            }
            clEvent.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    focusedIndex=position
                    itemView.animate().scaleX(1.1f).scaleY(1.05f).setDuration(200)
                        .withEndAction {
                            eventFragment.binding.rvEvent.scrollToPosition(absoluteAdapterPosition)
                            itemView.invalidate()
                            itemView.requestLayout()
                        }.start()
                    clEvent.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    clThumb.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    clThumb.setBackgroundResource(R.drawable.item_focused)

                } else {
                    itemView.animate().scaleX(1f).scaleY(1f).setDuration(200)
                        .start()
                    clEvent.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            com.otpview.R.color.transparent
                        )
                    )
                    clThumb.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            com.otpview.R.color.transparent
                        )
                    )

                }
            }


//            clEvent.remoteKey {
//                Log.e("smfsfms", "sncd b $it focusedIndex $focusedIndex")
//                when (it) {
//                    StreamEnum.UP_DPAD_KEY -> {
//                        if (eventFragment.binding.ivBack.isVisible) {
//                            eventFragment.binding.ivBack.requestFocus()
//                        }
//                    }
//
//                    StreamEnum.LEFT_DPAD_KEY -> {
//                        if (focusedIndex >= 1) {
//                            focusedIndex--
//                            notifyDataSetChanged()
//                        }
//
//                    }
//
//                    StreamEnum.RIGHT_DPAD_KEY -> {
//                        if (focusedIndex < eventList.size - 1) {
//                            focusedIndex++
//                            notifyDataSetChanged()
//                        }
//
//                    }
//
//                    else -> {}
//                }
//            }
//            clEvent.setOnKeyListener { v, keyCode, event ->
//                if (event.action == KeyEvent.ACTION_DOWN) {
//                    when (keyCode) {
//                        KeyEvent.KEYCODE_DPAD_UP -> {
//                            eventFragment.binding.ivBack.requestFocus()
//                        }
//
//                        KeyEvent.KEYCODE_DPAD_DOWN -> {
//                            eventFragment.binding.ivBack.requestFocus()
//                        }
//
//                        else -> {
//                        }
//                    }
//                }
//                false
//            }

//            if (position == focusedIndex) {
//                clEvent.requestFocus()
//               // eventFragment.binding.rvEvent.scrollToPosition(focusedIndex)
//            }
        }

    }

    fun updateDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())

        return try {
            val date = inputFormat.parse(inputDate)
            val formattedDate = outputFormat.format(date)
            formattedDate

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("sjcbnsjbc", "ncnvdj $e")
            ""
        }
    }

    class ProjectView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: AppCompatTextView = itemView.findViewById(R.id.tvTitle)
        val tvSubtitle: AppCompatTextView = itemView.findViewById(R.id.tvSubtitle)
        val tvProjectCount: AppCompatTextView = itemView.findViewById(R.id.tvProjectCount)
        val clEvent: ConstraintLayout = itemView.findViewById(R.id.clEvent)
        val clThumb: LinearLayout = itemView.findViewById(R.id.clThumb)
        val thumb: ImageView = itemView.findViewById(R.id.ivThumb)
        val tvAddEvent: TextView = itemView.findViewById(R.id.tvAddEvent)
        val mview: View = itemView.findViewById(R.id.view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectView {
        val inflate = context.layoutInflater.inflate(R.layout.projects_item, parent, false)
        return ProjectView(inflate)
    }

    fun update(newlist: ArrayList<ResponseItem>) {

        // eventList.addAll(newlist)
        notifyDataSetChanged()
    }

    fun addItem(newlist: ResponseItem) {
        Log.e("djjvdbv","vduvud $newlist")
        eventList.add(eventList.size-1,newlist)
       // notifyDataSetChanged()
//       notifyItemChanged(eventList.size-1)
    }
    fun pagination(newlist: ArrayList<ResponseItem>) {
        eventList.addAll(eventList.size - 1, newlist)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }
    override fun getItemCount(): Int = eventList.size
}