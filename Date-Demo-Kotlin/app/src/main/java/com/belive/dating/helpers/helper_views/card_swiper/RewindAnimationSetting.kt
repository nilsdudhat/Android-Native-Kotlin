package com.belive.dating.helpers.helper_views.card_swiper

import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator

class RewindAnimationSetting private constructor(
    private val direction: Direction,
    private val duration: Int,
    private val interpolator: Interpolator,
) : AnimationSetting {

    override fun getDirection(): Direction {
        return direction
    }

    override fun getDuration(): Int {
        return duration
    }

    override fun getInterpolator(): Interpolator {
        return interpolator
    }

    class Builder {
        private var direction: Direction = Direction.Bottom
        private var duration: Int = Duration.Normal.duration
        private var interpolator: Interpolator = DecelerateInterpolator()

        fun setDirection(direction: Direction): Builder {
            this.direction = direction
            return this
        }

        fun setDuration(duration: Int): Builder {
            this.duration = duration
            return this
        }

        fun setInterpolator(interpolator: Interpolator): Builder {
            this.interpolator = interpolator
            return this
        }

        fun build(): RewindAnimationSetting {
            return RewindAnimationSetting(
                direction,
                duration,
                interpolator
            )
        }
    }
}