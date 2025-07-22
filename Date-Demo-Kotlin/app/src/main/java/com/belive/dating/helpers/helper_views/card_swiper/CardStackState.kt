package com.belive.dating.helpers.helper_views.card_swiper

import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.extensions.logger

class CardStackState {
    var status: Status = Status.Idle
    var width: Int = 0
    var height: Int = 0
    var dx: Int = 0
    var dy: Int = 0
    var topPosition: Int = 0
    var targetPosition: Int = RecyclerView.NO_POSITION
    var proportion: Float = 0.0f

    enum class Status {
        Idle,
        Dragging,
        RewindAnimating,
        AutomaticSwipeAnimating,
        AutomaticSwipeAnimated,
        ManualSwipeAnimating,
        ManualSwipeAnimated;

        fun isBusy(): Boolean {
            return this != Idle
        }

        fun isDragging(): Boolean {
            return this == Dragging
        }

        fun isSwipeAnimating(): Boolean {
            return this == ManualSwipeAnimating || this == AutomaticSwipeAnimating
        }

        fun toAnimatedStatus(): Status {
            return when (this) {
                ManualSwipeAnimating -> ManualSwipeAnimated
                AutomaticSwipeAnimating -> AutomaticSwipeAnimated
                else -> Idle
            }
        }
    }

    fun next(state: Status) {
        status = state
    }

    fun getDirection(): Direction {
        return if (Math.abs(dy) < Math.abs(dx)) {
            if (dx < 0.0f) Direction.Left else Direction.Right
        } else {
            if (dy < 0.0f) Direction.Top else Direction.Bottom
        }
    }

    fun getRatio(): Float {
        val absDx = Math.abs(dx)
        val absDy = Math.abs(dy)
        val ratio: Float
        ratio = if (absDx < absDy) {
            absDy / (height / 2.0f)
        } else {
            absDx / (width / 2.0f)
        }
        return ratio.coerceAtMost(1.0f)
    }

    fun isSwipeCompleted(): Boolean {
        if (status.isSwipeAnimating()) {
            if (topPosition < targetPosition) {
                if (width < Math.abs(dx) || height < Math.abs(dy)) {
                    return true
                }
            }
        }
        return false
    }

    fun canScrollToPosition(position: Int, itemCount: Int): Boolean {
        logger("TAG", "canScrollToPosition: ==========$position=======$itemCount")
        return position != topPosition && position >= 0 && position <= itemCount && !status.isBusy()
    }
}