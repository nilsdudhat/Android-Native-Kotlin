package com.belive.dating.helpers.helper_views.card_swiper

import androidx.recyclerview.widget.RecyclerView

class CardStackDataObserver(private val recyclerView: RecyclerView) :
    RecyclerView.AdapterDataObserver() {

    override fun onChanged() {
        val manager = getCardStackLayoutManager()
        manager.setTopPosition(0)
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        // Do nothing
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        // Do nothing
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        // Do nothing
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        val manager = getCardStackLayoutManager()
        val topPosition = manager.getTopPosition()
        if (manager.itemCount == 0) {
            manager.setTopPosition(0)
        } else if (positionStart < topPosition) {
            val diff = topPosition - positionStart
            manager.setTopPosition(minOf(topPosition - diff, manager.itemCount - 1))
        }
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        val manager = getCardStackLayoutManager()
        manager.removeAllViews()
    }

    private fun getCardStackLayoutManager(): CardStackLayoutManager {
        val manager = recyclerView.layoutManager
        if (manager is CardStackLayoutManager) {
            return manager
        }
        throw IllegalStateException("CardStackView must be set CardStackLayoutManager.")
    }
}