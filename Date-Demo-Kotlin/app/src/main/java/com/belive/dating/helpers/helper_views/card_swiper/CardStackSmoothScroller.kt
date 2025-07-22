package com.belive.dating.helpers.helper_views.card_swiper

import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

class CardStackSmoothScroller(
    private val type: ScrollType,
    private val manager: CardStackLayoutManager
) : LinearSmoothScroller(manager.context1) {

    override fun onSeekTargetStep(dx: Int, dy: Int, state: RecyclerView.State, action: Action) {
        if (type == ScrollType.AutomaticRewind) {
            val setting = manager.getCardStackSetting().rewindAnimationSetting
            action.update(
                -getDx(setting),
                -getDy(setting),
                setting.getDuration(),
                setting.getInterpolator()
            )
        }
    }

    override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
        val x = targetView.translationX.toInt()
        val y = targetView.translationY.toInt()
        val setting: AnimationSetting
        when (type) {
            ScrollType.AutomaticSwipe -> {
                setting = manager.getCardStackSetting().swipeAnimationSetting
                action.update(
                    -getDx(setting),
                    -getDy(setting),
                    setting.getDuration(),
                    setting.getInterpolator()
                )
            }

            ScrollType.AutomaticRewind -> {
                setting = manager.getCardStackSetting().rewindAnimationSetting
                action.update(x, y, setting.getDuration(), setting.getInterpolator())
            }

            ScrollType.ManualSwipe -> {
                val dx = -x * 10
                val dy = -y * 10
                setting = manager.getCardStackSetting().swipeAnimationSetting
                action.update(dx, dy, setting.getDuration(), setting.getInterpolator())
            }

            ScrollType.ManualCancel -> {
                setting = manager.getCardStackSetting().rewindAnimationSetting
                action.update(x, y, setting.getDuration(), setting.getInterpolator())
            }
        }
    }

    override fun onStart() {
        val listener = manager.getCardStackListener()
        val state = manager.getCardStackState()
        when (type) {
            ScrollType.AutomaticSwipe -> {
                state.next(CardStackState.Status.AutomaticSwipeAnimating)
                manager.getTopView()
                    ?.let { listener.onCardDisappeared(it, manager.getTopPosition()) }
            }

            ScrollType.AutomaticRewind -> {
                state.next(CardStackState.Status.RewindAnimating)
            }

            ScrollType.ManualSwipe -> {
                state.next(CardStackState.Status.ManualSwipeAnimating)
                manager.getTopView()
                    ?.let { listener.onCardDisappeared(it, manager.getTopPosition()) }
            }

            ScrollType.ManualCancel -> {
                state.next(CardStackState.Status.RewindAnimating)
            }
        }
    }

    override fun onStop() {
        val listener = manager.getCardStackListener()
        when (type) {
            ScrollType.AutomaticSwipe -> {
                // Notify callback from CardStackLayoutManager
            }

            ScrollType.AutomaticRewind -> {
                listener.onCardRewound()
                manager.getTopView()?.let { listener.onCardAppeared(it, manager.getTopPosition()) }
            }

            ScrollType.ManualSwipe -> {
                // Notify callback from CardStackLayoutManager
            }

            ScrollType.ManualCancel -> {
                listener.onCardCanceled()
            }
        }
    }

    private fun getDx(setting: AnimationSetting): Int {
        val state = manager.getCardStackState()
        var dx = 0
        when (setting.getDirection()) {
            Direction.Left -> dx = -state.width * 2
            Direction.Right -> dx = state.width * 2
            Direction.Top, Direction.Bottom -> dx = 0
        }
        return dx
    }

    private fun getDy(setting: AnimationSetting): Int {
        val state = manager.getCardStackState()
        var dy = 0
        when (setting.getDirection()) {
            Direction.Left, Direction.Right -> dy = state.height / 4
            Direction.Top -> dy = -state.height * 2
            Direction.Bottom -> dy = state.height * 2
        }
        return dy
    }
}
