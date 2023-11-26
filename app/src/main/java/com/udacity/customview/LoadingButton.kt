package com.udacity.customview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.udacity.R
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var progressPercentage = 0F
    private var endPercentage = 0F

    private var animationDuration = 7000L
    private var endingAnimationDuration = 500L

    private var drawAnimation = true

    private var textSize = 0.0

    private var text = ""
    private var buttonText = ""
    private var loadingText = ""

    private var backgroundColor = 0
    private var textColor = 0
    private var progressCircleColor = 0
    private var progressBackgroundColor = 0

    private val animator: ValueAnimator = ValueAnimator.ofFloat(0f, 1F)

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Clicked -> {
                startLoadingAnimation(animationDuration)
                invalidate()
            }
            ButtonState.Completed -> {
                startLoadingAnimation(endingAnimationDuration)
            }
            ButtonState.Failed -> {
                startLoadingAnimation(endingAnimationDuration)
            }
            ButtonState.Loading -> {}
        }
    }

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            textSize = getDimensionPixelSize(R.styleable.LoadingButton_buttonTextSize, 0).toDouble()
            text = getString(R.styleable.LoadingButton_buttonText).toString()
            buttonText = text
            loadingText = getString(R.styleable.LoadingButton_loadingButtonText).toString()
            backgroundColor = getColor(R.styleable.LoadingButton_backgroundColor, 0)
            textColor = getColor(R.styleable.LoadingButton_textColor, 0)
            progressCircleColor = getColor(R.styleable.LoadingButton_progressCircleColor, 0)
            progressBackgroundColor = getColor(R.styleable.LoadingButton_progressBackgroundColor, 0)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackground(canvas)
        if (drawAnimation) {
            drawProgressBackground(canvas)
            drawProgressCircle(canvas)
        }
        drawText(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0,
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.save()
        canvas.drawColor(backgroundColor)
        canvas.restore()
    }

    private fun drawText(canvas: Canvas) {
        canvas.save()
        val x = width / 2F
        val y = height / 2F + 25F

        paint.color = textColor
        paint.textSize = textSize.toFloat()
        paint.textAlign = Paint.Align.CENTER

        canvas.drawText(text, x, y, paint)
        canvas.restore()
    }

    private fun drawProgressCircle(canvas: Canvas) {
        canvas.save()
        val radius = width.coerceAtMost(height) / 4F
        val x = width / 2F + paint.measureText(text) - 100F
        val y = height / 2F

        val rectF = RectF(x - radius, y - radius, x + radius, y + radius)

        val startAngle = 0F
        val sweepAngle = 360F * progressPercentage
        paint.color = progressCircleColor
        canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)
        canvas.restore()
    }

    private fun drawProgressBackground(canvas: Canvas) {
        canvas.save()
        paint.color = progressBackgroundColor
        val rectBackground = Rect(0, 0, (width * endPercentage).toInt(), height)
        canvas.drawRect(rectBackground, paint)
        canvas.restore()
    }

    private fun startLoadingAnimation(duration: Long) {
        animator.duration = duration

        animator.addUpdateListener { animation ->
            val animationPercentage = animation.animatedValue as Float
            progressPercentage = animationPercentage
            endPercentage = animationPercentage
            invalidate()
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                isEnabled = true
                text = buttonText
                drawAnimation = false
                invalidate()
            }

            override fun onAnimationStart(animation: Animator) {
                isEnabled = false
                text = loadingText
                drawAnimation = true
            }
        })

        animator.start()
    }
}
