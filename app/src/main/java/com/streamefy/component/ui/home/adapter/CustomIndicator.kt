package com.streamefy.component.ui.home.adapter

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.streamefy.R
import com.streamefy.component.ui.home.HomeFragment.Companion.homeFragment

class CustomIndicator(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private val selectedDrawable = ContextCompat.getDrawable(context, R.drawable.ic_indicator_unselected)
    private val unselectedDrawable = ContextCompat.getDrawable(context, R.drawable.ic_indicator_selected )

    init {
        orientation = VERTICAL
    }

    fun setIndicatorCount(count: Int, selectedPosition: Int) {
        removeAllViews()
        for (i in 0 until count) {
            val size = if (i == selectedPosition) {
                resources.getDimensionPixelSize(R.dimen.indicator_size_selected)
            } else {
                resources.getDimensionPixelSize(R.dimen.indicator_size_unselected)
            }

            val dot = View(context).apply {
                layoutParams = LayoutParams(size, size).apply {
                    setMargins(
                        0,
                        resources.getDimensionPixelSize(R.dimen.indicator_margin),
                        0,
                        resources.getDimensionPixelSize(R.dimen.indicator_margin)
                    )
                }
                background = if (i == selectedPosition) selectedDrawable else unselectedDrawable
                gravity=Gravity.CENTER
            }

            dot.setOnClickListener {
                Log.e("sjbjsbsd","click index $i")
                homeFragment.binding.rvBackgVideo.post {
                    homeFragment.binding.rvBackgVideo.smoothScrollToPosition(i)
                }
                //homeFragment.binding.rvBackgVideo.scrollToPosition(i)
            }
            addView(dot)
        }
    }

    fun updateIndicator(selectedPosition: Int) {

        for (i in 0 until childCount) {
            val dot = getChildAt(i)

            val size = if (i == selectedPosition) {
                resources.getDimensionPixelSize(R.dimen.indicator_size_selected)
            } else {
                resources.getDimensionPixelSize(R.dimen.indicator_size_unselected)
            }
            dot.background = if (i == selectedPosition) selectedDrawable else unselectedDrawable
            val layoutParams = dot.layoutParams as LayoutParams
            layoutParams.width = size
            layoutParams.height = size
            layoutParams.gravity=Gravity.CENTER
            dot.layoutParams = layoutParams
        }
    }
}
