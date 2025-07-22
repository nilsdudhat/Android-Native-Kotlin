package com.belive.dating.helpers.helper_functions.vertical_oval

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import com.belive.dating.R

class VerticalOvalLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var aspectRatio = 0.75f
    private var borderWidth = 0f
    private var borderColor = Color.WHITE
    private var spaceBetweenBorderAndContent = 0f
    private var guideColor = Color.WHITE
    private var guideWidth = 0f
    var showGuides: Boolean = true
        set(value) {
            field = value
            invalidate() // Force redraw when value changes
        }

    private val contentRect = RectF()
    private val borderRect = RectF()
    private val contentClipPath = Path()
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    init {
        setWillNotDraw(false)
        context.withStyledAttributes(attrs, R.styleable.VerticalOvalLayout, defStyleAttr, 0) {
            aspectRatio = getFloat(R.styleable.VerticalOvalLayout_widthToHeightRatio, 0.75f)
            borderWidth = getDimension(R.styleable.VerticalOvalLayout_borderWidth, 2f.dp)
            borderColor = getColor(R.styleable.VerticalOvalLayout_borderColor, Color.WHITE)
            spaceBetweenBorderAndContent = getDimension(
                R.styleable.VerticalOvalLayout_spaceBetweenBorderAndContent,
                8f.dp
            )
            guideColor = getColor(R.styleable.VerticalOvalLayout_guideColor, Color.WHITE)
            guideWidth = getDimension(R.styleable.VerticalOvalLayout_guideWidth, 2f.dp)
            showGuides = getBoolean(R.styleable.VerticalOvalLayout_showGuides, true)
        }

        borderPaint.strokeWidth = borderWidth
        borderPaint.color = borderColor
        guidePaint.strokeWidth = guideWidth
        guidePaint.color = guideColor
    }

    private val Float.dp: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            resources.displayMetrics
        )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val totalInset = (borderWidth + spaceBetweenBorderAndContent) * 2
        val contentWidth = (width - totalInset).toInt()
        val contentHeight = (contentWidth / aspectRatio).toInt()
        val totalHeight = (contentHeight + totalInset).toInt()

        setMeasuredDimension(width, totalHeight)

        val childWidthSpec = MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY)
        val childHeightSpec = MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.EXACTLY)

        (0 until childCount).forEach { i ->
            getChildAt(i).measure(childWidthSpec, childHeightSpec)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val contentInset = borderWidth + spaceBetweenBorderAndContent
        contentRect.set(
            contentInset,
            contentInset,
            w - contentInset,
            h - contentInset
        )

        val halfBorder = borderWidth / 2
        borderRect.set(
            halfBorder,
            halfBorder,
            w - halfBorder,
            h - halfBorder
        )

        contentClipPath.reset()
        contentClipPath.addOval(contentRect, Path.Direction.CW)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val inset = (borderWidth + spaceBetweenBorderAndContent).toInt()
        (0 until childCount).forEach { i ->
            getChildAt(i).layout(
                inset,
                inset,
                width - inset,
                height - inset
            )
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        // Draw clipped children
        canvas.save()
        canvas.clipPath(contentClipPath)
        super.dispatchDraw(canvas)
        canvas.restore()

        // Draw border
        canvas.drawOval(borderRect, borderPaint)

        // Draw guidelines
        if (showGuides) {
            drawGuidelines(canvas)
        }
    }

    private fun drawGuidelines(canvas: Canvas) {
        canvas.drawLine(
            contentRect.left,
            contentRect.centerY(),
            contentRect.right,
            contentRect.centerY(),
            guidePaint
        )

        canvas.drawLine(
            contentRect.centerX(),
            contentRect.top,
            contentRect.centerX(),
            contentRect.bottom,
            guidePaint
        )
    }
}