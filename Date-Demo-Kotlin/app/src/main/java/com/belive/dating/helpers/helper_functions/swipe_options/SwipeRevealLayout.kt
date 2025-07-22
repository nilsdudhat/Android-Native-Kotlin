package com.belive.dating.helpers.helper_functions.swipe_options

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.belive.dating.R

open class SwipeRevealLayout : ViewGroup {
    companion object {
        const val STATE_CLOSE = 0
        const val STATE_CLOSING = 1
        const val STATE_OPEN = 2
        const val STATE_OPENING = 3
        const val STATE_DRAGGING = 4

        const val DEAFULT_GLANCING_ANIMATION_PAUSE = 300L
        private const val DEFAULT_MIN_FLING_VELOCITY = 500 // dp per second
        private const val DEFAULT_MIN_DIST_REQUEST_DISALLOW_PARENT = 1 // dp

        const val DRAG_EDGE_NONE = 0
        const val DRAG_EDGE_LEFT = 0x1
        const val DRAG_EDGE_RIGHT = 0x1 shl 1
        const val DRAG_EDGE_TOP = 0x1 shl 2
        const val DRAG_EDGE_BOTTOM = 0x1 shl 3

        const val MODE_NORMAL = 0
        const val MODE_SAME_LEVEL = 1
    }

    interface DragStateChangeListener {
        fun onDragStateChanged(state: Int)
    }

    private lateinit var mMainView: View
    private val mRectMainClose = Rect()
    private var mMinDistRequestDisallowParent = 0
    private val revealableViewManager = RevealableViewManager()

    private var mGlancing = false
    @Volatile
    private var mAborted = false
    @Volatile
    private var mIsScrolling = false
    @Volatile
    private var mLockDrag = false

    private var mMinFlingVelocity = DEFAULT_MIN_FLING_VELOCITY
    @Volatile
    private var mState = STATE_CLOSE
    private var mMode = MODE_SAME_LEVEL

    private var mLastMainLeft = 0
    private var mLastMainTop = 0

    private var mDragEdge = DRAG_EDGE_LEFT or DRAG_EDGE_RIGHT
    private var mEnableEdge = DRAG_EDGE_LEFT or DRAG_EDGE_RIGHT or DRAG_EDGE_TOP or DRAG_EDGE_BOTTOM

    @Volatile
    private var currentDragEdge = DRAG_EDGE_NONE

    private lateinit var mDragHelper: ViewDragHelper
    private lateinit var mGestureDetector: GestureDetectorCompat

    private var mDragStateChangeListener: DragStateChangeListener? = null
    private var mSwipeListener: SwipeListener? = null

    private var mOnLayoutCount = 0

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> mSwipeListener?.onTouchUp(false)
            MotionEvent.ACTION_UP -> mSwipeListener?.onTouchUp(true)
        }

        mGestureDetector.onTouchEvent(event)
        mDragHelper.processTouchEvent(event)
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> mSwipeListener?.onTouchUp(false)
            MotionEvent.ACTION_UP -> mSwipeListener?.onTouchUp(true)
        }
        mDragHelper.processTouchEvent(ev)
        mGestureDetector.onTouchEvent(ev)

        val settling = mDragHelper.viewDragState == ViewDragHelper.STATE_SETTLING
        val idleAfterScrolled = mDragHelper.viewDragState == ViewDragHelper.STATE_IDLE && mIsScrolling

        return settling || idleAfterScrolled
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (childCount >= 2) {
            if (mDragEdge == (DRAG_EDGE_LEFT or DRAG_EDGE_RIGHT)) {
                revealableViewManager.putRevealableView(RevealableViewModel(getChildAt(0), DRAG_EDGE_LEFT))
                revealableViewManager.putRevealableView(RevealableViewModel(getChildAt(1), DRAG_EDGE_RIGHT))
            } else {
                revealableViewManager.putRevealableView(RevealableViewModel(getChildAt(0), DRAG_EDGE_TOP))
                revealableViewManager.putRevealableView(RevealableViewModel(getChildAt(1), DRAG_EDGE_BOTTOM))
            }
            mMainView = getChildAt(childCount - 1)
        } else if (childCount == 1) {
            mMainView = getChildAt(0)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (mState == STATE_DRAGGING) return

        mAborted = false

        for (index in 0 until childCount) {
            val child = getChildAt(index)

            var left = 0
            var right = 0
            var top = 0
            var bottom = 0

            val minLeft = paddingLeft
            val maxRight = maxOf(r - paddingRight - l, 0)
            val minTop = paddingTop
            val maxBottom = maxOf(b - paddingBottom - t, 0)

            var measuredChildHeight = child.measuredHeight
            var measuredChildWidth = child.measuredWidth

            val childParams = child.layoutParams
            var matchParentHeight = false
            var matchParentWidth = false

            if (childParams != null) {
                matchParentHeight = childParams.height == LayoutParams.MATCH_PARENT || childParams.height == LayoutParams.FILL_PARENT
                matchParentWidth = childParams.width == LayoutParams.MATCH_PARENT || childParams.width == LayoutParams.FILL_PARENT
            }

            if (matchParentHeight) {
                measuredChildHeight = maxBottom - minTop
                childParams.height = measuredChildHeight
            }

            if (matchParentWidth) {
                measuredChildWidth = maxRight - minLeft
                childParams.width = measuredChildWidth
            }

            var dragEdge = DRAG_EDGE_NONE
            val group = revealableViewManager.getGroupFromView(child)
            if (group != null) dragEdge = group.dragEdge

            when (dragEdge) {
                DRAG_EDGE_RIGHT -> {
                    left = maxOf(r - measuredChildWidth - paddingRight - l, minLeft)
                    top = minOf(paddingTop, maxBottom)
                    right = maxOf(r - paddingRight - l, minLeft)
                    bottom = minOf(measuredChildHeight + paddingTop, maxBottom)
                }

                DRAG_EDGE_LEFT, DRAG_EDGE_NONE -> {
                    left = minOf(paddingLeft, maxRight)
                    top = minOf(paddingTop, maxBottom)
                    right = minOf(measuredChildWidth + paddingLeft, maxRight)
                    bottom = minOf(measuredChildHeight + paddingTop, maxBottom)
                }

                DRAG_EDGE_TOP -> {
                    left = maxOf(r - measuredChildWidth - paddingRight - l, minLeft)
                    top = minOf(paddingTop, maxBottom + 1)
                    right = maxOf(r - paddingRight - l, minLeft)
                    bottom = minOf(measuredChildHeight + paddingTop, maxBottom)
                }

                DRAG_EDGE_BOTTOM -> {
                    left = minOf(paddingLeft, maxRight)
                    top = minOf(paddingTop, maxBottom + 2)
                    right = minOf(measuredChildWidth + paddingLeft, maxRight)
                    bottom = minOf(measuredChildHeight + paddingTop, maxBottom)
                }
            }

            child.layout(left, top, right, bottom)

            if (mMode == MODE_SAME_LEVEL) {
                when (dragEdge) {
                    DRAG_EDGE_LEFT -> group?.view?.offsetLeftAndRight(-group.width)
                    DRAG_EDGE_RIGHT -> group?.view?.offsetLeftAndRight(group.width)
                    DRAG_EDGE_TOP -> group?.view?.offsetTopAndBottom(-group.height)
                    DRAG_EDGE_BOTTOM -> group?.view?.offsetTopAndBottom(group.height)
                }
            }
        }

        initRects()

        mLastMainLeft = mMainView.left
        mLastMainTop = mMainView.top

        mOnLayoutCount++
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (childCount < 2) {
            throw RuntimeException("Layout must have two children")
        }

        val params = layoutParams

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measuredHeight = MeasureSpec.getSize(heightMeasureSpec)

        var desiredWidth = 0
        var desiredHeight = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            if (i == childCount - 1) {
                desiredWidth = maxOf(child.measuredWidth, desiredWidth)
                desiredHeight = maxOf(child.measuredHeight, desiredHeight)
            }
        }

        for (i in 0 until childCount - 1) {
            val child = getChildAt(i)
            val childParams = child.layoutParams

            val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(desiredWidth, MeasureSpec.AT_MOST)
            val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(desiredHeight, MeasureSpec.AT_MOST)

            child.measure(newWidthMeasureSpec, newHeightMeasureSpec)
        }

        desiredWidth += paddingLeft + paddingRight
        desiredHeight += paddingTop + paddingBottom

        when (widthMode) {
            MeasureSpec.EXACTLY -> desiredWidth = measuredWidth
            else -> {
                if (params.width == LayoutParams.MATCH_PARENT) {
                    desiredWidth = measuredWidth
                }
                if (widthMode == MeasureSpec.AT_MOST) {
                    desiredWidth = minOf(desiredWidth, measuredWidth)
                }
            }
        }

        when (heightMode) {
            MeasureSpec.EXACTLY -> desiredHeight = measuredHeight
            else -> {
                if (params.height == LayoutParams.MATCH_PARENT) {
                    desiredHeight = measuredHeight
                }
                if (heightMode == MeasureSpec.AT_MOST) {
                    desiredHeight = minOf(desiredHeight, measuredHeight)
                }
            }
        }

        setMeasuredDimension(desiredWidth, desiredHeight)
    }

    override fun computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    fun open(animation: Boolean) {
        mAborted = false

        val rect = revealableViewManager.getMainOpenRect(mRectMainClose, currentDragEdge)

        if (animation) {
            mState = STATE_OPENING
            mDragHelper.smoothSlideViewTo(mMainView, rect.left, rect.top)
            mDragStateChangeListener?.onDragStateChanged(mState)
        } else {
            mState = STATE_OPEN
            mDragHelper.abort()
            mMainView.layout(rect.left, rect.top, rect.right, rect.bottom)
        }

        ViewCompat.postInvalidateOnAnimation(this)
    }

    fun close(animation: Boolean) {
        mAborted = false

        if (animation) {
            mState = STATE_CLOSING
            mDragHelper.smoothSlideViewTo(mMainView, mRectMainClose.left, mRectMainClose.top)
            mDragStateChangeListener?.onDragStateChanged(mState)
        } else {
            mState = STATE_CLOSE
            mDragHelper.abort()
            mMainView.layout(mRectMainClose.left, mRectMainClose.top, mRectMainClose.right, mRectMainClose.bottom)
        }

        ViewCompat.postInvalidateOnAnimation(this)
    }

    fun doCornerGlanceAnimation(dragEdge: Int, percentage: Float) {
        require(percentage in 0.0..1.0) { "doCornerGlanceAnimation(dragEdge, percentage). percentage should be 0<=p<=1. p: $percentage" }

        mGlancing = true

        val rect = revealableViewManager.getMainOpenRect(mRectMainClose, dragEdge)
        mDragHelper.smoothSlideViewTo(mMainView, (rect.left * percentage).toInt(), rect.top)
        ViewCompat.postInvalidateOnAnimation(this)
    }

    fun setMinFlingVelocity(velocity: Int) {
        mMinFlingVelocity = velocity
    }

    fun getMinFlingVelocity(): Int = mMinFlingVelocity

    fun setDragEdge(dragEdge: Int) {
        mDragEdge = dragEdge
    }

    fun getDragEdge(): Int = mDragEdge

    fun setSwipeListener(listener: SwipeListener?) {
        mSwipeListener = listener
    }

    fun setEnableEdge(edges: Int) {
        mEnableEdge = edges
    }

    fun setLockDrag(lock: Boolean) {
        mLockDrag = lock
    }

    fun isDragLocked(): Boolean = mLockDrag

    fun isOpened(): Boolean = mState == STATE_OPEN

    fun isClosed(): Boolean = mState == STATE_CLOSE

    internal fun setDragStateChangeListener(listener: DragStateChangeListener?) {
        mDragStateChangeListener = listener
    }

    fun abort() {
        mAborted = true
        mDragHelper.abort()
    }

    fun shouldRequestLayout(): Boolean = mOnLayoutCount < childCount

    private fun initRects() {
        mRectMainClose.set(
            mMainView.left,
            mMainView.top,
            mMainView.right,
            mMainView.bottom
        )
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.SwipeRevealLayout, 0, 0).apply {
                try {
                    mDragEdge = getInteger(R.styleable.SwipeRevealLayout_dragEdge, DRAG_EDGE_LEFT or DRAG_EDGE_RIGHT)
                    if (!(mDragEdge != (DRAG_EDGE_LEFT or DRAG_EDGE_RIGHT) || mDragEdge != (DRAG_EDGE_TOP or DRAG_EDGE_BOTTOM))) {
                        throw IllegalArgumentException("Currently only support vertical or horizontal dragging edges")
                    }
                    mMinFlingVelocity = getInteger(R.styleable.SwipeRevealLayout_flingVelocity, DEFAULT_MIN_FLING_VELOCITY)
                    mMode = getInteger(R.styleable.SwipeRevealLayout_mode, MODE_NORMAL)

                    mMinDistRequestDisallowParent = getDimensionPixelSize(
                        R.styleable.SwipeRevealLayout_minDistRequestDisallowParent,
                        dpToPx(DEFAULT_MIN_DIST_REQUEST_DISALLOW_PARENT)
                    )
                } finally {
                    recycle()
                }
            }
        }

        mDragHelper = ViewDragHelper.create(this, 1.0f, mDragHelperCallback)
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_ALL)
        mGestureDetector = GestureDetectorCompat(context, mGestureListener)
    }

    private val mGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        private var hasDisallowed = false

        override fun onDown(e: MotionEvent): Boolean {
            mIsScrolling = false
            hasDisallowed = false
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            mIsScrolling = true
            return false
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            mIsScrolling = true
            return false
        }
    }

    private val mDragHelperCallback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            mAborted = false
            if (mLockDrag) return false
            mDragHelper.captureChildView(mMainView, pointerId)
            return false
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            when {
                (mDragEdge and DRAG_EDGE_TOP) > 0 && top > mRectMainClose.top && (mEnableEdge and DRAG_EDGE_TOP) > 0 -> {
                    currentDragEdge = DRAG_EDGE_TOP
                    return revealableViewManager.getGroupFromEdge(DRAG_EDGE_TOP)?.let { group ->
                        top.coerceIn(mRectMainClose.top, mRectMainClose.top + group.height)
                    } ?: child.top
                }

                (mDragEdge and DRAG_EDGE_BOTTOM) > 0 && top < mRectMainClose.top && (mEnableEdge and DRAG_EDGE_BOTTOM) > 0 -> {
                    currentDragEdge = DRAG_EDGE_BOTTOM
                    return revealableViewManager.getGroupFromEdge(DRAG_EDGE_BOTTOM)?.let { group ->
                        top.coerceIn(mRectMainClose.top - group.height, mRectMainClose.top)
                    } ?: child.top
                }

                else -> return child.top
            }
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            when {
                (mDragEdge and DRAG_EDGE_LEFT) > 0 && left > mRectMainClose.left && (mEnableEdge and DRAG_EDGE_LEFT) > 0 -> {
                    currentDragEdge = DRAG_EDGE_LEFT
                    return revealableViewManager.getGroupFromEdge(DRAG_EDGE_LEFT)?.let { group ->
                        left.coerceIn(mRectMainClose.left, mRectMainClose.left + group.width)
                    } ?: child.left
                }

                (mDragEdge and DRAG_EDGE_RIGHT) > 0 && left < mRectMainClose.left && (mEnableEdge and DRAG_EDGE_RIGHT) > 0 -> {
                    currentDragEdge = DRAG_EDGE_RIGHT
                    return revealableViewManager.getGroupFromEdge(DRAG_EDGE_RIGHT)?.let { group ->
                        left.coerceIn(mRectMainClose.left - group.width, mRectMainClose.left)
                    } ?: child.left
                }

                else -> return child.left
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val velRightExceeded = pxToDp(xvel.toInt()) >= mMinFlingVelocity
            val velLeftExceeded = pxToDp(xvel.toInt()) <= -mMinFlingVelocity
            val velUpExceeded = pxToDp(yvel.toInt()) <= -mMinFlingVelocity
            val velDownExceeded = pxToDp(yvel.toInt()) >= mMinFlingVelocity

            val pivotHorizontal = getHalfwayPivotHorizontal()
            val pivotVertical = getHalfwayPivotVertical()

            when (currentDragEdge and mEnableEdge) {
                DRAG_EDGE_RIGHT -> when {
                    velRightExceeded -> close(true)
                    velLeftExceeded -> open(true)
                    else -> if (mMainView.right < pivotHorizontal) open(true) else close(true)
                }

                DRAG_EDGE_LEFT -> when {
                    velRightExceeded -> open(true)
                    velLeftExceeded -> close(true)
                    else -> if (mMainView.left < pivotHorizontal) close(true) else open(true)
                }

                DRAG_EDGE_TOP -> when {
                    velUpExceeded -> close(true)
                    velDownExceeded -> open(true)
                    else -> if (mMainView.top < pivotVertical) close(true) else open(true)
                }

                DRAG_EDGE_BOTTOM -> when {
                    velUpExceeded -> open(true)
                    velDownExceeded -> close(true)
                    else -> if (mMainView.bottom < pivotVertical) open(true) else close(true)
                }
            }
        }

        override fun onEdgeDragStarted(edgeFlags: Int, pointerId: Int) {
            if (mLockDrag) return

            val edgeStartLeft = (mDragEdge == DRAG_EDGE_RIGHT) && edgeFlags == ViewDragHelper.EDGE_LEFT
            val edgeStartRight = (mDragEdge == DRAG_EDGE_LEFT) && edgeFlags == ViewDragHelper.EDGE_RIGHT
            val edgeStartTop = (mDragEdge == DRAG_EDGE_BOTTOM) && edgeFlags == ViewDragHelper.EDGE_TOP
            val edgeStartBottom = (mDragEdge == DRAG_EDGE_TOP) && edgeFlags == ViewDragHelper.EDGE_BOTTOM

            if (edgeStartLeft || edgeStartRight || edgeStartTop || edgeStartBottom) {
                mDragHelper.captureChildView(mMainView, pointerId)
            }
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            if (mMode == MODE_SAME_LEVEL) {
                when {
                    (mDragEdge and (DRAG_EDGE_LEFT or DRAG_EDGE_RIGHT)) != 0 -> revealableViewManager.offsetLeftAndRight(dx)
                    (mDragEdge and (DRAG_EDGE_TOP or DRAG_EDGE_BOTTOM)) != 0 -> {
                        revealableViewManager.offsetTopAndBottom(dy)
                        parent?.requestDisallowInterceptTouchEvent(dy != 0)
                    }
                }
            }

            val isMoved = mMainView.left != mLastMainLeft || mMainView.top != mLastMainTop
            mSwipeListener?.let {
                when {
                    mMainView.left == mRectMainClose.left && mMainView.top == mRectMainClose.top -> it.onClosed(this@SwipeRevealLayout)
                    getSlideOffset() == 1.0f -> it.onOpened(this@SwipeRevealLayout, DRAG_EDGE_LEFT)
                    getSlideOffset() == -1.0f -> it.onOpened(this@SwipeRevealLayout, DRAG_EDGE_RIGHT)
                    else -> it.onSlide(this@SwipeRevealLayout, getSlideOffset())
                }
            }

            mLastMainLeft = mMainView.left
            mLastMainTop = mMainView.top
            ViewCompat.postInvalidateOnAnimation(this@SwipeRevealLayout)
        }

        override fun onViewDragStateChanged(state: Int) {
            val prevState = mState
            mState = when (state) {
                ViewDragHelper.STATE_DRAGGING -> STATE_DRAGGING
                ViewDragHelper.STATE_IDLE -> {
                    if (mGlancing) {
                        mGlancing = false
                        handler?.postDelayed({
                            mDragHelper.smoothSlideViewTo(mMainView, mRectMainClose.left, mRectMainClose.top)
                            ViewCompat.postInvalidateOnAnimation(this@SwipeRevealLayout)
                        }, DEAFULT_GLANCING_ANIMATION_PAUSE)
                        return
                    }

                    when {
                        (mDragEdge and (DRAG_EDGE_LEFT or DRAG_EDGE_RIGHT)) != 0 -> {
                            if (mMainView.left == mRectMainClose.left) STATE_CLOSE else STATE_OPEN
                        }

                        (mDragEdge and (DRAG_EDGE_TOP or DRAG_EDGE_BOTTOM)) != 0 -> {
                            if (mMainView.top == mRectMainClose.top) STATE_CLOSE else STATE_OPEN
                        }

                        else -> mState
                    }
                }

                else -> mState
            }

            if (!mAborted && prevState != mState) {
                mDragStateChangeListener?.onDragStateChanged(mState)
            }
        }
    }

    private fun getHalfwayPivotHorizontal(): Int = when (currentDragEdge) {
        DRAG_EDGE_LEFT -> mRectMainClose.left + (revealableViewManager.getGroupFromEdge(DRAG_EDGE_LEFT)?.width ?: 0) / 2
        DRAG_EDGE_RIGHT -> mRectMainClose.right - (revealableViewManager.getGroupFromEdge(DRAG_EDGE_RIGHT)?.width ?: 0) / 2
        else -> 0
    }

    private fun getHalfwayPivotVertical(): Int = when (currentDragEdge) {
        DRAG_EDGE_TOP -> mRectMainClose.top + (revealableViewManager.getGroupFromEdge(DRAG_EDGE_TOP)?.height ?: 0) / 2
        DRAG_EDGE_BOTTOM -> mRectMainClose.bottom - (revealableViewManager.getGroupFromEdge(DRAG_EDGE_BOTTOM)?.height ?: 0) / 2
        else -> 0
    }

    private fun getSlideOffset(): Float = when (currentDragEdge) {
        DRAG_EDGE_LEFT -> (mMainView.left - mRectMainClose.left).toFloat() / (revealableViewManager.getGroupFromEdge(DRAG_EDGE_LEFT)?.width
            ?: 1).toFloat()

        DRAG_EDGE_RIGHT -> (mMainView.left - mRectMainClose.left).toFloat() / (revealableViewManager.getGroupFromEdge(DRAG_EDGE_RIGHT)?.width
            ?: 1).toFloat()

        else -> 0f
    }

    private fun pxToDp(px: Int): Int {
        val metrics = context.resources.displayMetrics
        return (px / (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    private fun dpToPx(dp: Int): Int {
        val metrics = context.resources.displayMetrics
        return (dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }
}