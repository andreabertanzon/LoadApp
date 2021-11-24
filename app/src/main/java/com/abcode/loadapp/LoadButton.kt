package com.abcode.loadapp

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.properties.Delegates

class LoadButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttribute: Int = 0
): View(context, attrs, defStyleAttribute) {
    
    // PRIVATE SETUP VALUES
    private var widthDim = 0
    private var heightDim = 0
    private var buttonText: String
    private var buttonBg = R.attr.buttonBackgroundColor
    private var progress: Float = 0f
    private var valueAnimator = ValueAnimator()
    private val textRectangle = Rect()

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LoadButton,
            0, 0
        ).apply {

            try {
                buttonText = getString(R.styleable.LoadButton_text).toString()
                buttonBg = ContextCompat.getColor(context, R.color.purple_500)
            } finally {
                recycle()
            }
        }
    }
    
    // BTN State to decide loading or not
    private var btnState: BtnState by Delegates.observable(BtnState.Done) { _, _, newValue ->
        when(newValue) {
            BtnState.Loading -> {
                setText("Downloading...")
                setBgColor("#3700B3")
                valueAnimator= ValueAnimator.ofFloat(0f, 1f).apply {
                    addUpdateListener {
                        progress = animatedValue as Float
                        invalidate()
                    }
                    repeatMode = ValueAnimator.REVERSE
                    repeatCount = ValueAnimator.INFINITE
                    duration = 3000
                    start()
                }
            }

            BtnState.Done -> {
                setText("Download")
                setBgColor("#07C2AA")
                valueAnimator.cancel()
                resetProgress()
            }
        }
        invalidate()
    }
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 50.0f
        color = Color.WHITE
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.purple_500)
    }

    private val inProgressBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.purple_700)
    }

    private val inProgressArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cornerRadius = 10.0f
        val backgroundWidth = measuredWidth.toFloat()
        val backgroundHeight = measuredHeight.toFloat()

        canvas.drawColor(buttonBg)
        textPaint.getTextBounds(buttonText, 0, buttonText.length, textRectangle)
        canvas.drawRoundRect(0f, 0f, backgroundWidth, backgroundHeight, cornerRadius, cornerRadius, backgroundPaint)

        if (btnState == BtnState.Loading) {
            var progressVal = progress * measuredWidth.toFloat()
            canvas.drawRoundRect(0f, 0f, progressVal, backgroundHeight, cornerRadius, cornerRadius, inProgressBackgroundPaint)

            val arcDiameter = cornerRadius * 2
            val arcRectSize = measuredHeight.toFloat() - paddingBottom.toFloat() - arcDiameter

            progressVal = progress * 360f
            canvas.drawArc(paddingStart + arcDiameter,
                    paddingTop.toFloat() + arcDiameter,
                    arcRectSize,
                    arcRectSize,
                    0f,
                    progressVal,
                    true,
                    inProgressArcPaint)
        }
        val centerX = measuredWidth.toFloat() / 2
        val centerY = measuredHeight.toFloat() / 2 - textRectangle.centerY()

        canvas.drawText(buttonText,centerX, centerY, textPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val width: Int = resolveSizeAndState(minWidth, widthMeasureSpec, 1)
        val height: Int = resolveSizeAndState(
                MeasureSpec.getSize(width),
                heightMeasureSpec,
                0
        )
        widthDim = width
        heightDim = height
        setMeasuredDimension(width, height)
    }

    fun setLoadBtnState(state: BtnState) {
        btnState = state
    }

    private fun setText(buttonText: String) {
        this.buttonText = buttonText
        invalidate()
        requestLayout()
    }

    private fun setBgColor(backgroundColor: String) {
        buttonBg = Color.parseColor(backgroundColor)
        invalidate()
        requestLayout()
    }

    private fun resetProgress() {
        progress = 0f
    }
}