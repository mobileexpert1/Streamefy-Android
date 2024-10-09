package com.streamefy.component.ui.otp

import com.mukeshsolanki.OtpView
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.streamefy.R

class CustomOTPView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : OtpView(context, attrs, defStyleAttr) {

    var otpViewItemCount = 3
    var otpViewItemSpacing = 10
    var otpViewItemHeight = 45
    var otpViewItemWidth = 45
    private var activeIcon: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.ic_selected_inputfiled)
    private var inactiveIcon: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.bg_round_stroke_gray)
    private val paint: Paint = Paint()
    private val backgroundPaint: Paint = Paint()
    private val borderPaint: Paint = Paint()
    private val cornerRadius = 50f // Set your desired corner radius
    private val rectF: RectF = RectF()
    private val textPaint: TextPaint = TextPaint()

    init {
        backgroundPaint.style = Paint.Style.FILL
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 2f
        textPaint.textSize = 20f // Set your desired text size
        textPaint.textAlign = Paint.Align.CENTER
        val typeface = ResourcesCompat.getFont(context, R.font.filsonpro_regular)
        textPaint.typeface = typeface
        isFocusable = true
        isFocusableInTouchMode = true
    }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            requestFocus()
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
        return super.onTouchEvent(event)
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw each OTP box with the corresponding icon
        for (i in 0 until otpViewItemCount) {
            val left = i * (otpViewItemWidth + otpViewItemSpacing)
            val top = (height - otpViewItemHeight) / 2f
            val right = left + otpViewItemWidth
            val bottom = top + otpViewItemHeight

            rectF.set(left.toFloat(), top, right.toFloat(), bottom)

            // Set background color
            backgroundPaint.color = if (i < text?.length ?: 0) {
                ContextCompat.getColor(context, com.otpview.R.color.transparent)
            } else {
                ContextCompat.getColor(context, com.otpview.R.color.transparent)
            }
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, backgroundPaint)

            // Draw border
            borderPaint.color = if (i < text?.length ?: 0) {
                ContextCompat.getColor(context, R.color.light_gray)
            } else {
                ContextCompat.getColor(context, R.color.purple)
            }
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, borderPaint)

            // Draw the character
            val character = if (i < text?.length ?: 0) text!![i].toString() else ""
            drawCenteredText(canvas, character, left.toFloat(), top, otpViewItemWidth, otpViewItemHeight)

        }

        // Draw cursor if it should be visible
        drawCursor(canvas)

    }

    private fun drawIcon(canvas: Canvas, icon: Drawable?, left: Float, top: Float) {
        if (icon != null) {
            val iconLeft = left + (otpViewItemWidth - icon.intrinsicWidth) / 2
            val iconTop = top + (otpViewItemHeight - icon.intrinsicHeight) / 2
            icon.setBounds(
                iconLeft.toInt(),
                iconTop.toInt(),
                (iconLeft + icon.intrinsicWidth).toInt(),
                (iconTop + icon.intrinsicHeight).toInt()
            )
            icon.draw(canvas)
        }
    }

    private fun drawCursor(canvas: Canvas) {
        if (isCursorVisible) {
            val cursorIndex = selectionStart
            if (cursorIndex in 0 until otpViewItemCount) {
                val left = cursorIndex * (otpViewItemWidth + otpViewItemSpacing) + otpViewItemWidth / 2 - lineWidth / 2
                val top = (height - otpViewItemHeight) / 2f
                val right = left + lineWidth
                val bottom = top + otpViewItemHeight

                paint.color = ContextCompat.getColor(context, R.color.purple)
                canvas.drawRect(left.toFloat(), top, right.toFloat(), bottom, paint)
            }
        }
    }
    private fun drawCenteredText(canvas: Canvas, text: String, left: Float, top: Float, width: Int, height: Int) {
        if (text.isNotEmpty()) {
            val textWidth = textPaint.measureText(text)
            val textX = left + (width / 2) // Center horizontally
            val textY = top + (height / 2) - ((textPaint.descent() + textPaint.ascent()) / 2) // Center vertically
            canvas.drawText(text, textX, textY, textPaint)
        }
    }
    fun onOtpCompleted() {
        if (text?.length == otpViewItemCount) {
            Toast.makeText(context, "OTP Completed: ${text.toString()}", Toast.LENGTH_SHORT).show()
        }
    }
}