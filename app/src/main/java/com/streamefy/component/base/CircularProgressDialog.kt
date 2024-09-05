package com.streamefy.component.base

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.streamefy.R
import com.streamefy.databinding.DialogProgressCircularBinding
import com.streamefy.databinding.ExitDialogBinding

class CircularProgressDialog(context: Context) : BaseDialog<DialogProgressCircularBinding>(
    context,
    R.layout.dialog_progress_circular,
    R.style.TransparentDialogTheme
) {

    override fun setupViews() {
        binding.apply {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                val wrapDrawable = DrawableCompat.wrap(progressBar.getIndeterminateDrawable())
                DrawableCompat.setTint(
                    wrapDrawable,
                    ContextCompat.getColor(context, R.color.primary)
                )
                progressBar.setIndeterminateDrawable(DrawableCompat.unwrap<Drawable>(wrapDrawable))
            } else {
                progressBar.getIndeterminateDrawable().setColorFilter(
                    ContextCompat.getColor(context, R.color.primary),
                    PorterDuff.Mode.SRC_IN
                )
            }
        }
    }
}