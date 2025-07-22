package com.belive.dating.helpers.helper_views.card_swiper

enum class Direction {
    Left,
    Right,
    Top,
    Bottom;

    companion object {
        val HORIZONTAL = listOf(Left, Right, Top)
        val HORIZONTAL_LIKE = listOf(Left, Right)
        val HORIZONTAL_SUPER_LIKE = listOf(Left, Top)
        val HORIZONTAL_BOTH = listOf(Left)
        val VERTICAL = listOf(Top)
        val FREEDOM = entries
    }
}