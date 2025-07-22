package com.belive.dating.helpers.helper_views.card_swiper

import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.annotation.FloatRange
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.R
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger

class CardStackLayoutManager(private val context: Context) : RecyclerView.LayoutManager(), RecyclerView.SmoothScroller.ScrollVectorProvider {
    var context1: Context? = null

    private var listener = CardStackListener.DEFAULT
    private val setting = CardStackSetting()
    private val state = CardStackState()

    constructor(context: Context, listener: CardStackListener) : this(context) {
        context1 = context
        this.listener = listener
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, s: RecyclerView.State) {
        update(recycler)
        if (s.didStructureChange()) {
            val topView = getTopView()
            if (topView != null) {
                listener.onCardAppeared(getTopView()!!, state.topPosition)
            }
        }
    }

    override fun canScrollHorizontally(): Boolean {
        return setting.swipeableMethod.canSwipe() && setting.canScrollHorizontal
    }

    override fun canScrollVertically(): Boolean {
        return setting.swipeableMethod.canSwipe() && setting.canScrollVertical
    }

    override fun scrollHorizontallyBy(
        dx: Int, recycler: RecyclerView.Recycler, s: RecyclerView.State?
    ): Int {
        if (state.topPosition == itemCount) {
            return 0
        }
        when (state.status) {
            CardStackState.Status.Idle -> if (setting.swipeableMethod.canSwipeManually()) {
                state.dx -= dx
                update(recycler)
                return dx
            }

            CardStackState.Status.Dragging -> if (setting.swipeableMethod.canSwipeManually()) {
                state.dx -= dx
                update(recycler)
                return dx
            }

            CardStackState.Status.RewindAnimating -> {
                state.dx -= dx
                update(recycler)
                return dx
            }

            CardStackState.Status.AutomaticSwipeAnimating -> if (setting.swipeableMethod.canSwipeAutomatically()) {
                state.dx -= dx
                update(recycler)
                return dx
            }

            CardStackState.Status.AutomaticSwipeAnimated -> {}
            CardStackState.Status.ManualSwipeAnimating -> if (setting.swipeableMethod.canSwipeManually()) {
                state.dx -= dx
                update(recycler)
                return dx
            }

            CardStackState.Status.ManualSwipeAnimated -> {}
        }
        return 0
    }

    override fun scrollVerticallyBy(
        dy: Int, recycler: RecyclerView.Recycler, s: RecyclerView.State?
    ): Int {
        if (state.topPosition == itemCount) {
            return 0
        }
        when (state.status) {
            CardStackState.Status.Idle -> if (setting.swipeableMethod.canSwipeManually()) {
                state.dy -= dy
                update(recycler)
                return dy
            }

            CardStackState.Status.Dragging -> {
                logger("TAG", "Dragging: =======1=====" + state.getDirection())
                if (setting.swipeableMethod.canSwipeManually()) {
                    state.dy -= dy
                    update(recycler)
                    return dy
                }
            }

            CardStackState.Status.RewindAnimating -> {
                logger("TAG", "RewindAnimating: =======1=====$dy")
                state.dy -= dy
                update(recycler)
                return dy
            }

            CardStackState.Status.AutomaticSwipeAnimating -> if (setting.swipeableMethod.canSwipeAutomatically()) {
                state.dy -= dy
                update(recycler)
                return dy
            }

            CardStackState.Status.AutomaticSwipeAnimated -> {}
            CardStackState.Status.ManualSwipeAnimating ->
//                logger("TAG", "ManualSwipeAnimating: =======1=====" + dy);
                if (setting.swipeableMethod.canSwipeManually()) {
                    state.dy -= dy
                    //                    logger("TAG", "ManualSwipeAnimating: ======2======" + dy);
                    update(recycler)
                    return dy
                }

            CardStackState.Status.ManualSwipeAnimated -> {}
        }
        return 0
    }

    override fun onScrollStateChanged(s: Int) {
        when (s) {
            RecyclerView.SCROLL_STATE_IDLE -> if (state.targetPosition == RecyclerView.NO_POSITION) {
                // Swipeが完了した場合の処理
                state.next(CardStackState.Status.Idle)
                state.targetPosition = RecyclerView.NO_POSITION
            } else if (state.topPosition == state.targetPosition) {
                // Rewindが完了した場合の処理
                state.next(CardStackState.Status.Idle)
                state.targetPosition = RecyclerView.NO_POSITION
            } else {
                // 2枚以上のカードを同時にスワイプする場合の処理
                if (state.topPosition < state.targetPosition) {
                    // 1枚目のカードをスワイプすると一旦SCROLL_STATE_IDLEが流れる
                    // そのタイミングで次のアニメーションを走らせることで連続でスワイプしているように見せる
                    smoothScrollToNext(state.targetPosition)
                } else {
                    // Nextの場合と同様に、1枚目の処理が完了したタイミングで次のアニメーションを走らせる
                    smoothScrollToPrevious(state.targetPosition)
                }
            }

            RecyclerView.SCROLL_STATE_DRAGGING -> if (setting.swipeableMethod.canSwipeManually()) {
                state.next(CardStackState.Status.Dragging)
            }

            RecyclerView.SCROLL_STATE_SETTLING -> {}
        }
    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        return null
    }

    override fun scrollToPosition(position: Int) {
        if (setting.swipeableMethod.canSwipeAutomatically()) {
            if (state.canScrollToPosition(position, itemCount)) {
                state.topPosition = position
                requestLayout()
            }
        }
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?, s: RecyclerView.State?, position: Int
    ) {
        if (setting.swipeableMethod.canSwipeAutomatically()) {
            if (state.canScrollToPosition(position, itemCount)) {
                smoothScrollToPosition(position)
            }
        }
    }

    fun getCardStackSetting(): CardStackSetting {
        return setting
    }

    fun getCardStackState(): CardStackState {
        return state
    }

    fun getCardStackListener(): CardStackListener {
        return listener
    }

    fun updateProportion(x: Float, y: Float) {
        logger("--card_stack--", "updateProportion: $x, $y")

        if (getTopPosition() < itemCount) {
            val view = findViewByPosition(getTopPosition())
            if (view != null) {
                val half = height / 2.0f
                state.proportion = -(y - half - view.top) / half
            }
        }
    }

    private fun update(recycler: RecyclerView.Recycler) {
        try {
            state.width = width
            state.height = height
            if (state.isSwipeCompleted()) {
                removeAndRecycleView(getTopView()!!, recycler)
                val direction = state.getDirection()
                state.next(state.status.toAnimatedStatus())
                state.topPosition++
                state.dx = 0
                state.dy = 0
                if (state.topPosition == state.targetPosition) {
                    state.targetPosition = RecyclerView.NO_POSITION
                }
                Handler(Looper.getMainLooper()).post {
                    listener.onCardSwiped(direction)
                    val topView = getTopView()
                    if (topView != null) {
                        listener.onCardAppeared((getTopView())!!, state.topPosition)
                    }
                }
            }
            val direction = state.getDirection()
            detachAndScrapAttachedViews(recycler)
            val parentTop = paddingTop
            val parentLeft = paddingLeft
            val parentRight = width - paddingLeft
            val parentBottom = height - paddingBottom
            var i = state.topPosition
            while (i < state.topPosition + setting.visibleCount && i < itemCount) {
                val child = recycler.getViewForPosition(i)
                addView(child, 0)
                measureChildWithMargins(child, 0, 0)
                layoutDecoratedWithMargins(child, parentLeft, parentTop, parentRight, parentBottom)
                resetTranslation(child)
                resetScale(child)
                resetRotation(child)
                resetOverlay(child)
                if (i == state.topPosition) {
                    updateTranslation(child)
                    resetScale(child)
                    updateRotation(child)
                    updateOverlay(child)
                } else {
                    if (direction == Direction.Top) {
                    } else {
                        val currentIndex = i - state.topPosition
                        updateTranslation(child, currentIndex)
                        updateScale(child, currentIndex)
                        resetRotation(child)
                        resetOverlay(child)
                    }
                }
                i++
            }
            if (state.status.isDragging()) {
                listener.onCardDragging(state.getDirection(), state.getRatio())
            }
        } catch (e: Exception) {
            catchLog("update: " + gsonString(e))
        }
    }

    private fun updateTranslation(view: View) {
        view.translationX = state.dx.toFloat()
        view.translationY = state.dy.toFloat()
    }

    private fun dpToPx(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    private fun updateTranslation(view: View, index: Int) {
        val nextIndex = index - 1
        val translationPx = dpToPx(context, setting.translationInterval)
        val currentTranslation = (index * translationPx).toFloat()
        val nextTranslation = (nextIndex * translationPx).toFloat()
        val targetTranslation = currentTranslation - (currentTranslation - nextTranslation) * state.getRatio()
        when (setting.stackFrom) {
            StackFrom.None -> {}
            StackFrom.Top -> view.translationY = -targetTranslation
            StackFrom.TopAndLeft -> {
                view.translationY = -targetTranslation
                view.translationX = -targetTranslation
            }

            StackFrom.TopAndRight -> {
                view.translationY = -targetTranslation
                view.translationX = targetTranslation
            }

            StackFrom.Bottom -> view.translationY = targetTranslation
            StackFrom.BottomAndLeft -> {
                view.translationY = targetTranslation
                view.translationX = -targetTranslation
            }

            StackFrom.BottomAndRight -> {
                view.translationY = targetTranslation
                view.translationX = targetTranslation
            }

            StackFrom.Left -> view.translationX = -targetTranslation
            StackFrom.Right -> view.translationX = targetTranslation
        }
    }

    private fun resetTranslation(view: View) {
        view.translationX = 0.0f
        view.translationY = 0.0f
    }

    private fun updateScale(view: View, index: Int) {
        val nextIndex = index - 1
        val currentScale = 1.0f - index * (1.0f - setting.scaleInterval)
        val nextScale = 1.0f - nextIndex * (1.0f - setting.scaleInterval)
        val targetScale = currentScale + (nextScale - currentScale) * state.getRatio()
        when (setting.stackFrom) {
            StackFrom.None -> {
                view.scaleX = targetScale
                view.scaleY = targetScale
            }

            StackFrom.Top -> view.scaleX = targetScale
            StackFrom.TopAndLeft -> view.scaleX = targetScale
            StackFrom.TopAndRight -> view.scaleX = targetScale
            StackFrom.Bottom -> view.scaleX = targetScale
            StackFrom.BottomAndLeft -> view.scaleX = targetScale
            StackFrom.BottomAndRight -> view.scaleX = targetScale
            StackFrom.Left -> view.scaleY = targetScale
            StackFrom.Right -> view.scaleY = targetScale
        }
    }

    private fun resetScale(view: View) {
        view.scaleX = 1.0f
        view.scaleY = 1.0f
    }

    private fun updateRotation(view: View) {
        val degree = state.dx * setting.maxDegree / width * state.proportion
        view.rotation = degree
    }

    private fun resetRotation(view: View) {
        view.rotation = 0.0f
    }

    private fun updateOverlay(view: View) {
        val leftOverlay = view.findViewById<View>(R.id.left_overlay)
        if (leftOverlay != null) {
            leftOverlay.alpha = 0.0f
        }
        val rightOverlay = view.findViewById<View>(R.id.right_overlay)
        if (rightOverlay != null) {
            rightOverlay.alpha = 0.0f
        }
        val topOverlay = view.findViewById<View>(R.id.top_overlay)
        if (topOverlay != null) {
            topOverlay.alpha = 0.0f
        }
        val bottomOverlay = view.findViewById<View>(R.id.bottom_overlay)
        if (bottomOverlay != null) {
            bottomOverlay.alpha = 0.0f
        }
        val direction = state.getDirection()
        val alpha: Float = setting.overlayInterpolator.getInterpolation(state.getRatio())
        when (direction) {
            Direction.Left -> if (leftOverlay != null) {
                leftOverlay.alpha = alpha
            }

            Direction.Right -> if (rightOverlay != null) {
                rightOverlay.alpha = alpha
            }

            Direction.Top -> if (topOverlay != null) {
                topOverlay.alpha = alpha
            }

            Direction.Bottom -> if (bottomOverlay != null) {
                bottomOverlay.alpha = alpha
            }
        }
    }

    private fun resetOverlay(view: View) {
        val leftOverlay = view.findViewById<View>(R.id.left_overlay)
        if (leftOverlay != null) {
            leftOverlay.alpha = 0.0f
        }
        val rightOverlay = view.findViewById<View>(R.id.right_overlay)
        if (rightOverlay != null) {
            rightOverlay.alpha = 0.0f
        }
        val topOverlay = view.findViewById<View>(R.id.top_overlay)
        if (topOverlay != null) {
            topOverlay.alpha = 0.0f
        }
        val bottomOverlay = view.findViewById<View>(R.id.bottom_overlay)
        if (bottomOverlay != null) {
            bottomOverlay.alpha = 0.0f
        }
    }

    fun smoothScrollToPosition(position: Int) {
        if (state.topPosition < position) {
            smoothScrollToNext(position)
        } else {
            smoothScrollToPrevious(position)
        }
    }

    private fun smoothScrollToNext(position: Int) {
        state.proportion = 0.0f
        state.targetPosition = position
        val scroller = CardStackSmoothScroller(ScrollType.AutomaticSwipe, this)
        scroller.targetPosition = state.topPosition
        startSmoothScroll(scroller)
    }

    private fun smoothScrollToPrevious(position: Int) {
        try {
            val topView = getTopView()
            if (topView != null) {
                listener.onCardDisappeared(getTopView()!!, state.topPosition)
            }
            state.proportion = 0.0f
            state.targetPosition = position
            state.topPosition--
            val scroller = CardStackSmoothScroller(ScrollType.AutomaticRewind, this)
            scroller.targetPosition = state.topPosition
            startSmoothScroll(scroller)
        } catch (e: Exception) {
            catchLog("smoothScrollToPrevious: " + gsonString(e))
        }
    }

    fun getTopView(): View? {
        return findViewByPosition(state.topPosition)
    }

    fun getTopPosition(): Int {
        return state.topPosition
    }

    fun setTopPosition(topPosition: Int) {
        state.topPosition = topPosition
    }

    fun setStackFrom(stackFrom: StackFrom) {
        setting.stackFrom = stackFrom
    }

    fun setVisibleCount(visibleCount: Int) {
        require(visibleCount >= 1) { "VisibleCount must be greater than 0." }
        setting.visibleCount = visibleCount
    }

    fun setTranslationInterval(@FloatRange(from = 0.0) translationInterval: Float) {
        if (translationInterval < 0.0f) {
            throw IllegalArgumentException("TranslationInterval must be greater than or equal 0.0f")
        }
        setting.translationInterval = translationInterval
    }

    fun setScaleInterval(@FloatRange(from = 0.0) scaleInterval: Float) {
        if (scaleInterval < 0.0f) {
            throw IllegalArgumentException("ScaleInterval must be greater than or equal 0.0f.")
        }
        setting.scaleInterval = scaleInterval
    }

    fun setSwipeThreshold(@FloatRange(from = 0.0, to = 1.0) swipeThreshold: Float) {
        if (swipeThreshold < 0.0f || 1.0f < swipeThreshold) {
            throw IllegalArgumentException("SwipeThreshold must be 0.0f to 1.0f.")
        }
        setting.swipeThreshold = swipeThreshold
    }

    fun setMaxDegree(@FloatRange(from = (-360.0f).toDouble(), to = 360.0) maxDegree: Float) {
        if (maxDegree < -360.0f || 360.0f < maxDegree) {
            throw IllegalArgumentException("MaxDegree must be -360.0f to 360.0f")
        }
        setting.maxDegree = maxDegree
    }

    fun setDirections(directions: List<Direction>) {
        setting.directions = directions
    }

    fun setCanScrollHorizontal(canScrollHorizontal: Boolean) {
        setting.canScrollHorizontal = canScrollHorizontal
    }

    fun setCanScrollVertical(canScrollVertical: Boolean) {
        setting.canScrollVertical = canScrollVertical
    }

    fun setSwipeableMethod(swipeableMethod: SwipeableMethod?) {
        if (swipeableMethod != null) {
            setting.swipeableMethod = swipeableMethod
        }
    }

    fun setSwipeAnimationSetting(swipeAnimationSetting: SwipeAnimationSetting) {
        setting.swipeAnimationSetting = swipeAnimationSetting
    }

    fun setRewindAnimationSetting(rewindAnimationSetting: RewindAnimationSetting) {
        setting.rewindAnimationSetting = rewindAnimationSetting
    }

    fun setOverlayInterpolator(overlayInterpolator: LinearInterpolator) {
        setting.overlayInterpolator = overlayInterpolator
    }
}