package com.streamefy.component.ui.projects

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.streamefy.R
import com.streamefy.component.base.StreamEnum
import com.streamefy.component.ui.projects.EventFragment.Companion.eventFragment
import com.streamefy.component.ui.projects.EventFragment.Companion.focusedIndex
import com.streamefy.component.ui.projects.EventFragment.Companion.isDark
import com.streamefy.component.ui.projects.model.ResponseItem
import com.streamefy.utils.gone
import com.streamefy.utils.loadAny
import com.streamefy.utils.loadUrl
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
//            itemView.isFocusable = true
//            itemView.isClickable = true
            Log.e("membercheck", "newlist ${data.isLast}")
            Log.e("DrawableCheck", "build version ${Build.VERSION.SDK_INT}")
            if (data.isLast){
                thumb.setBackgroundColor(ContextCompat.getColor(context,R.color.black))
                thumb.loadAny(R.drawable.ic_add_event)
                tvAddEvent.visible()
                tvTitle.gone()
                tvProjectCount.gone()
                tvSubtitle.gone()
                mview.gone()
                tvResetPin.gone()
               // line.visible()
                clEvent.setOnClickListener {
                    callBack.invoke(position, StreamEnum.LAST_EVENT)
                }
            }
            else {
                tvAddEvent.gone()
                tvTitle.visible()
                tvProjectCount.visible()
                tvSubtitle.visible()
                mview.visible()
                tvTitle.text = data.name
//            tvSubtitle.text = data.createDate
                tvProjectCount.text = data.mediaCount.toString()
                var date =updateDate(data.createDate)
                tvSubtitle.text = date

                if (data.thumbnail != null) {
                    // Picasso.get().load(data.thumbnail).into(thumb)
                    thumb.loadUrl(data.thumbnail)
                }
                clEvent.setOnClickListener {
                    callBack.invoke(position, StreamEnum.SINGLE)
                }

            }
            if (data.isPrimary) {
                tvResetPin.visible()
                tvResetPin.apply {

                    setOnClickListener {
                        callBack.invoke(position, StreamEnum.RESET_PIN)
                    }
                    setOnFocusChangeListener { _, hasFocus ->
                        if (hasFocus) {
                             setBackgroundResource(R.drawable.ic_button_selector)
//                             setBackgroundResource(R.drawable.ic_reset_pin_background)
                            eventFragment.binding.rvEvent.scrollToPosition(
                                absoluteAdapterPosition
                            )
                                setTextColor(ContextCompat.getColor(context, R.color.white))
                                val colorStateList = ColorStateList.valueOf(Color.WHITE)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    setCompoundDrawableTintList(colorStateList);
                                }else{
                                    val drawables: Array<Drawable> = getCompoundDrawables()
                                    if (drawables[0] != null) {

                                        DrawableCompat.setTint(drawables[0], Color.WHITE)
                                        setCompoundDrawablesWithIntrinsicBounds(
                                            drawables[0],
                                            null,
                                            null,
                                            null
                                        );
                                    }
                                }

                        }
                        else {
                            setBackgroundResource(R.drawable.ic_reset_pin_background)

                            if (isDark){

                                     setTextColor(ContextCompat.getColor(context, R.color.white))
                                    val colorStateList = ColorStateList.valueOf(Color.WHITE)
                                Log.e("DrawableCheck", "build version ${Build.VERSION.SDK_INT}")
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                       setCompoundDrawableTintList(colorStateList)
                                    }else{
                                        val drawables: Array<Drawable> = getCompoundDrawables()
                                        if (drawables[0] != null) {
                                            val drawableLeft = DrawableCompat.wrap(drawables[0])
                                            drawableLeft.setTint(Color.WHITE)

                                            setCompoundDrawablesWithIntrinsicBounds(
                                                drawableLeft,
                                                null,
                                                null,
                                                null
                                            );
                                        } else{
                                            Log.e("DrawableCheck", "Drawable Left: ${drawables}")
                                        }
                                    }
                                }
                            else{
                                    setTextColor(ContextCompat.getColor(context, R.color.black))
                                    val colorStateList = ColorStateList.valueOf(Color.BLACK)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                      setCompoundDrawableTintList(colorStateList);  // Apply the tint color to all compound drawables
                                    }else{
                                        val drawables: Array<Drawable> = getCompoundDrawables()
                                        if (drawables[0] != null) {
//                                            DrawableCompat.setTint(drawables[0], Color.BLACK)
                                            val drawableLeft = DrawableCompat.wrap(drawables[0])
                                            drawableLeft.setTint(Color.BLACK)

                                            setCompoundDrawablesWithIntrinsicBounds(
                                                drawableLeft,
                                                null,
                                                null,
                                                null
                                            );
                                        }
                                    }
                                }
                        }


//                        if (hasFocus) {
//                            toShowBackButton()
//                            focusView = VideoEnum.BACK_TO_VIDEO
//                            val params = ivBack.layoutParams as ConstraintLayout.LayoutParams
//                            params.width = resources.getDimensionPixelSize(R.dimen._62sdp) // Original size
//                            params.height = resources.getDimensionPixelSize(R.dimen._33sdp)
//                            ivBack.layoutParams = params
//                        } else {
//                            val params = ivBack.layoutParams as ConstraintLayout.LayoutParams
//                            params.width = resources.getDimensionPixelSize(R.dimen._60sdp) // Original size
//                            params.height = resources.getDimensionPixelSize(R.dimen._31sdp)
//                            ivBack.layoutParams = params
//                        }

                }}

                if (isDark){
                    tvResetPin.also {
                        it. setTextColor(ContextCompat.getColor(context, R.color.white))
                        val colorStateList = ColorStateList.valueOf(Color.WHITE)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            it.setCompoundDrawableTintList(colorStateList);  // Apply the tint color to all compound drawables
                        }else{
                            val drawables: Array<Drawable> = it.getCompoundDrawables()
                            if (drawables[0] != null) {
                                val drawableLeft = DrawableCompat.wrap(drawables[0])
//                                DrawableCompat.setTint(drawableLeft, Color.WHITE)
                                drawableLeft.setTint(Color.WHITE)
                                it.setCompoundDrawablesWithIntrinsicBounds(
                                    drawableLeft,
                                    null,
                                    null,
                                    null
                                );
                            }
                            else{
                                Log.e("DrawableCheck", "Drawable Left: ${drawables}")
                            }
                        }
                    }
                }
                else{
                    tvResetPin.also {
                        it. setTextColor(ContextCompat.getColor(context, R.color.black))
                        val colorStateList = ColorStateList.valueOf(Color.BLACK)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            it.setCompoundDrawableTintList(colorStateList);  // Apply the tint color to all compound drawables
                        }else{
                            val drawables: Array<Drawable> = it.getCompoundDrawables()
                            if (drawables[0] != null) {
                                val drawableLeft = DrawableCompat.wrap(drawables[0])
                                drawableLeft.setTint(Color.BLACK)
                               // DrawableCompat.setTint(drawableLeft, Color.BLACK)
                                it.setCompoundDrawablesWithIntrinsicBounds(
                                    drawableLeft,
                                    null,
                                    null,
                                    null
                                );
                            }
                        }
                    }

                }

            }
            else{
                tvResetPin.gone()
            }

//            tvResetPin.setOnFocusChangeListener { _, hasFocus ->
//                if (hasFocus) {
//                    tvResetPin. setBackgroundResource(R.drawable.ic_button_selector)
//                    eventFragment.binding.rvEvent.scrollToPosition(
//                        absoluteAdapterPosition
//                    )
////                            animate().scaleX(1.03f).scaleY(1f).setDuration(200)
////                                .withEndAction {
////                                    itemView.post {
////                                        itemView.requestLayout()
////                                        eventFragment.binding.rvEvent.scrollToPosition(
////                                            absoluteAdapterPosition
////                                        )
////                                    }
////                                }.start()
//                } else {
//                    tvResetPin. setBackgroundColor(
//                        ContextCompat.getColor(
//                            context,
//                            com.otpview.R.color.transparent
//                        )
//                    )
////                            animate().scaleX(1f).scaleY(1f).setDuration(200).start()
//                }
//            }
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
        val tvResetPin: TextView = itemView.findViewById(R.id.tvResetPin)
        val mview: View = itemView.findViewById(R.id.view)
        val line: View = itemView.findViewById(R.id.line)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectView {
        var inflator= LayoutInflater.from(context)
        val inflate = inflator.inflate(R.layout.projects_item, parent, false)
        return ProjectView(inflate)
    }

    fun update(newlist: ArrayList<ResponseItem>) {

        // eventList.addAll(newlist)
        notifyDataSetChanged()
    }
    fun updateAuth(index: Int) {
        eventList[index].isAuthorize=false
        notifyItemChanged(index)
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

//    override fun getItemViewType(position: Int): Int {
//        return super.getItemViewType(position)
//    }
//
//    override fun getItemId(position: Int): Long {
//        return super.getItemId(position)
//    }
    override fun getItemCount(): Int = eventList.size
}