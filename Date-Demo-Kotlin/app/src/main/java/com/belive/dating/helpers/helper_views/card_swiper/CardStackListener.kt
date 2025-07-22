package com.belive.dating.helpers.helper_views.card_swiper

import android.view.View

interface CardStackListener {
    fun onCardDragging(direction: Direction, ratio: Float)
    fun onCardSwiped(direction: Direction)
    fun onCardRewound()
    fun onCardCanceled()
    fun onCardAppeared(view: View, position: Int)
    fun onCardDisappeared(view: View, position: Int)

    companion object {
        val DEFAULT = object : CardStackListener {
            override fun onCardDragging(direction: Direction, ratio: Float) {}
            override fun onCardSwiped(direction: Direction) {}
            override fun onCardRewound() {}
            override fun onCardCanceled() {}
            override fun onCardAppeared(view: View, position: Int) {}
            override fun onCardDisappeared(view: View, position: Int) {}
        }
    }
}