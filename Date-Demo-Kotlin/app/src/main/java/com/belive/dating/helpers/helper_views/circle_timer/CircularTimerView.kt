package com.belive.dating.helpers.helper_views.circle_timer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.CountDownTimer
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import com.belive.dating.R
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.gsonString
import androidx.core.graphics.toColorInt

class CircularTimerView : View {

    private lateinit var progressBarPaint: Paint
    private lateinit var progressBarBackgroundPaint: Paint
    private lateinit var backgroundPaint: Paint
    private lateinit var textPaint: TextPaint

    private var mRadius: Float = 0f
    private val mArcBounds = RectF()

    private var drawUpto = 0f

    private var progressColor = Color.BLUE
    private var progressBackgroundColor = Color.GRAY
    private var backgroundColor = Color.GRAY
    private var strokeWidthDimension = 1f
    private var backgroundWidth = 1f
    private var roundedCorners = false
    private var maxValue = 100f
    private var progressTextColor = Color.BLACK
    private var textSize = 5f
    private var text = ""
    private var suffix = ""
    private var prefix = ""
    private var isClockwise = false
    private var startingAngle = 0
    var defStyleAttr = 0

    private var circularTimerListener: CircularTimerListener? = null
    private var countDownTimer: CountDownTimer? = null

    constructor(context: Context) : super(context) {
        initPaints(context)
    }

    private fun initPaints(context: Context) {
        progressBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = progressColor
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthDimension * context.resources.displayMetrics.density
            strokeCap = if (roundedCorners) Paint.Cap.ROUND else Paint.Cap.BUTT
        }

        progressBarBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = progressBackgroundColor
            style = Paint.Style.STROKE
            strokeWidth = backgroundWidth * context.resources.displayMetrics.density
            strokeCap = Paint.Cap.SQUARE
        }

        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = backgroundColor
        }

        textPaint = TextPaint().apply {
            color = progressTextColor
            textSize = this@CircularTimerView.textSize
            isAntiAlias = true
        }
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initPaints(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        initPaints(context, attrs)
    }

    private fun initPaints(context: Context, attrs: AttributeSet) {
        try {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.CircularTimerView, defStyleAttr, 0)

            progressColor = ta.getColor(R.styleable.CircularTimerView_progressColor, Color.BLUE)
            backgroundColor = ta.getColor(R.styleable.CircularTimerView_backgroundColor, Color.GRAY)
            progressBackgroundColor = ta.getColor(R.styleable.CircularTimerView_progressBackgroundColor, Color.GRAY)

            strokeWidthDimension = ta.getFloat(R.styleable.CircularTimerView_strokeWidthDimension, 1f)
            backgroundWidth = ta.getFloat(R.styleable.CircularTimerView_backgroundWidth, 1f)
            roundedCorners = ta.getBoolean(R.styleable.CircularTimerView_roundedCorners, false)
            maxValue = ta.getFloat(R.styleable.CircularTimerView_maxValue, 100f)
            progressTextColor = ta.getColor(R.styleable.CircularTimerView_progressTextColor, Color.BLACK)
            textSize = ta.getDimension(R.styleable.CircularTimerView_textSize, 5f)
            suffix = ta.getString(R.styleable.CircularTimerView_suffix)!!
            prefix = ta.getString(R.styleable.CircularTimerView_prefix)!!
            text = ta.getString(R.styleable.CircularTimerView_progressText)!!
            isClockwise = ta.getBoolean(R.styleable.CircularTimerView_isClockwise, true)
            startingAngle = ta.getInt(R.styleable.CircularTimerView_startingPoint, 0)

            progressBarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            progressBarPaint.style = Paint.Style.FILL
            progressBarPaint.color = progressColor
            progressBarPaint.style = Paint.Style.STROKE
            progressBarPaint.strokeWidth = strokeWidthDimension * resources.displayMetrics.density
            if (roundedCorners) {
                progressBarPaint.strokeCap = Paint.Cap.ROUND
            } else {
                progressBarPaint.strokeCap = Paint.Cap.BUTT
            }
            val pc = String.format("#%06X", 0xFFFFFF and progressColor)
            progressBarPaint.color = pc.toColorInt()

            progressBarBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            progressBarBackgroundPaint.style = Paint.Style.FILL
            progressBarBackgroundPaint.color = progressBackgroundColor
            progressBarBackgroundPaint.style = Paint.Style.STROKE
            progressBarBackgroundPaint.strokeWidth = backgroundWidth * resources.displayMetrics.density
            progressBarBackgroundPaint.strokeCap = Paint.Cap.SQUARE
            val bc = String.format("#%06X", 0xFFFFFF and progressBackgroundColor)
            progressBarBackgroundPaint.color = bc.toColorInt()

            backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            backgroundPaint.style = Paint.Style.FILL
            backgroundPaint.color = backgroundColor
            val bcfill = String.format("#%06X", 0xFFFFFF and backgroundColor)
            backgroundPaint.color = bcfill.toColorInt()

            ta.recycle()

            textPaint = TextPaint()
            textPaint.color = progressTextColor
            val c = String.format("#%06X", 0xFFFFFF and progressTextColor)
            textPaint.color = c.toColorInt()
            textPaint.textSize = textSize
            textPaint.isAntiAlias = true
        } catch (e: Exception) {
            catchLog("initPaints: ${gsonString(e)}")
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mRadius = w.coerceAtMost(h) / 2f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val size = w.coerceAtMost(h)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val mouthInset = mRadius / 3
        canvas.drawCircle(mRadius, mRadius, mouthInset * 2, backgroundPaint)

        mArcBounds.set(mouthInset, mouthInset, mRadius * 2 - mouthInset, mRadius * 2 - mouthInset)
        canvas.drawArc(mArcBounds, 0f, 360f, false, progressBarBackgroundPaint)

        val sweepAngle = if (isClockwise) drawUpto / maxValue * 360 else drawUpto / maxValue * -360
        canvas.drawArc(mArcBounds, startingAngle.toFloat(), sweepAngle, false, progressBarPaint)

        if (!TextUtils.isEmpty(suffix)) {
            suffix = ""
        }

        if (!TextUtils.isEmpty(prefix)) {
            prefix = ""
        }

        val drawnText = prefix + text + suffix

        if (!TextUtils.isEmpty(text)) {
            val textHeight = textPaint.descent() + textPaint.ascent()
            canvas.drawText(
                drawnText, (width - textPaint.measureText(drawnText)) / 2.0f, (width - textHeight) / 2.0f, textPaint
            )
        }
    }

    override fun onDetachedFromWindow() {
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
        }
        super.onDetachedFromWindow()
    }

    fun setProgress(f: Float) {
        drawUpto = f
        invalidate()
    }

    fun getProgress(): Float {
        return drawUpto
    }

    fun getProgressPercentage(): Float {
        return drawUpto / maxValue * 100
    }

    fun setProgressColor(color: Int) {
        progressColor = color
        progressBarPaint.color = color
        invalidate()
    }

    fun setProgressColor(color: String?) {
        color?.let { progressBarPaint.color = it.toColorInt() }
        invalidate()
    }

    fun setProgressBackgroundColor(color: Int) {
        progressBackgroundColor = color
        progressBarBackgroundPaint.color = color
        invalidate()
    }

    fun setProgressBackgroundColor(color: String?) {
        color?.let { progressBarBackgroundPaint.color = it.toColorInt() }
        invalidate()
    }

    fun setBackgroundColors(color: Int) {
        backgroundColor = color
        backgroundPaint.color = color
        invalidate()
    }

    fun setBackgroundColor(color: String?) {
        color?.let { backgroundPaint.color = it.toColorInt() }
        invalidate()
    }

    fun getMaxValue(): Float {
        return maxValue
    }

    fun setMaxValue(max: Float) {
        maxValue = max
        invalidate()
    }

    fun setStrokeWidthDimension(width: Float) {
        strokeWidthDimension = width
        invalidate()
    }

    fun getStrokeWidthDimension(): Float {
        return strokeWidthDimension
    }

    fun setBackgroundWidth(width: Float) {
        backgroundWidth = width
        invalidate()
    }

    fun getBackgroundWidth(): Float {
        return backgroundWidth
    }

    fun setText(progressText: String) {
        text = progressText
        invalidate()
    }

    fun getText(): String {
        return text
    }

    fun setTextColor(color: Int) {
        progressTextColor = color
        textPaint.color = color
        invalidate()
    }

    fun setTextColor(color: String?) {
        color?.let { textPaint.color = it.toColorInt() }
        invalidate()
    }

    fun getTextColor(): Int {
        return progressTextColor
    }

    fun setSuffix(suffix: String?) {
        this.suffix = suffix!!
        invalidate()
    }

    fun getSuffix(): String {
        return suffix
    }

    fun getPrefix(): String {
        return prefix
    }

    fun setPrefix(prefix: String?) {
        this.prefix = prefix!!
        invalidate()
    }

    fun getClockwise(): Boolean {
        return isClockwise
    }

    fun setClockwise(clockwise: Boolean) {
        isClockwise = clockwise
        invalidate()
    }

    fun getStartingAngle(): Int {
        return startingAngle
    }

    /**
     * @param startingAngle 270 for Top
     * 0 for Right
     * 90 for Bottom
     * 180 for Left
     */
    fun setStartingAngle(startingAngle: Int) {
        this.startingAngle = startingAngle
        invalidate()
    }

    fun setCircularTimerListener(
        circularTimerListener: CircularTimerListener, time: Long, timeFormatEnum: TimeFormatEnum
    ) {
        this.circularTimerListener = circularTimerListener

        var timeInMillis = 0L
        val intervalDuration = 1000L

        when (timeFormatEnum) {
            TimeFormatEnum.MILLIS -> timeInMillis = time
            TimeFormatEnum.SECONDS -> timeInMillis = time * 1000
            TimeFormatEnum.MINUTES -> timeInMillis = time * 1000 * 60
            TimeFormatEnum.HOUR -> timeInMillis = time * 1000 * 60 * 60
            TimeFormatEnum.DAY -> timeInMillis = time * 1000 * 60 * 60 * 24
        }

        countDownTimer?.cancel()
        val maxTime = timeInMillis
        countDownTimer = object : CountDownTimer(maxTime, intervalDuration) {
            override fun onTick(l: Long) {
                val percentTimeCompleted = ((maxTime - l) / maxTime.toDouble())
                drawUpto = (maxValue * percentTimeCompleted).toFloat()
                text = circularTimerListener.updateDataOnTick(l).toString()
                invalidate()
            }

            override fun onFinish() {
                val percentTimeCompleted = 1.0
                drawUpto = (maxValue * percentTimeCompleted).toFloat()
                circularTimerListener.onTimerFinished()
                invalidate()
            }
        }
    }

    fun setCircularTimerListener(
        circularTimerListener: CircularTimerListener, time: Long, timeFormatEnum: TimeFormatEnum, timeInterval: Long
    ) {
        this.circularTimerListener = circularTimerListener

        var timeInMillis = 0L

        when (timeFormatEnum) {
            TimeFormatEnum.MILLIS -> timeInMillis = time
            TimeFormatEnum.SECONDS -> timeInMillis = time * 1000
            TimeFormatEnum.MINUTES -> timeInMillis = time * 1000 * 60
            TimeFormatEnum.HOUR -> timeInMillis = time * 1000 * 60 * 60
            TimeFormatEnum.DAY -> timeInMillis = time * 1000 * 60 * 60 * 24
        }

        countDownTimer?.cancel()
        val maxTime = timeInMillis
        countDownTimer = object : CountDownTimer(maxTime, timeInterval) {
            override fun onTick(l: Long) {
                val percentTimeCompleted = ((maxTime - l) / maxTime.toDouble())
                drawUpto = (maxValue * percentTimeCompleted).toFloat()
                text = circularTimerListener.updateDataOnTick(l).toString()
                invalidate()
            }

            override fun onFinish() {
                val percentTimeCompleted = 1.0
                drawUpto = (maxValue * percentTimeCompleted).toFloat()
                text = circularTimerListener.updateDataOnTick(0).toString()
                circularTimerListener.onTimerFinished()
                invalidate()
            }
        }
    }

    fun startTimer(): Boolean {
        countDownTimer?.start()
        return countDownTimer != null
    }
}