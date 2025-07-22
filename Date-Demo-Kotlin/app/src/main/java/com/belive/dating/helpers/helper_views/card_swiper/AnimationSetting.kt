package com.belive.dating.helpers.helper_views.card_swiper

import android.view.animation.Interpolator

interface AnimationSetting {
    fun getDirection(): Direction
    fun getDuration(): Int
    fun getInterpolator(): Interpolator
}