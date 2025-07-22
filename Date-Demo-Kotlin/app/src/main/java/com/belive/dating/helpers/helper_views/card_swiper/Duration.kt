package com.belive.dating.helpers.helper_views.card_swiper

enum class Duration(val duration: Int) {
    Fast(100),
    Normal(200),
    Slow(500);

    companion object {
        fun fromVelocity(velocity: Int): Duration {
            return when {
                velocity < 1000 -> Slow
                velocity < 5000 -> Normal
                else -> Fast
            }
        }
    }
}