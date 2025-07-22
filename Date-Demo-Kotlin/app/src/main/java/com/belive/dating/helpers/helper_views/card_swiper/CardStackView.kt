package com.belive.dating.helpers.helper_views.card_swiper

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.extensions.logger

class CardStackView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    RecyclerView(context, attrs, defStyle) {

    private var observer = CardStackDataObserver(this)

    init {
        initialize()
    }

    override fun setLayoutManager(manager: LayoutManager?) {
        if (manager is CardStackLayoutManager) {
            super.setLayoutManager(manager)
        } else {
            throw IllegalArgumentException("CardStackView must be set CardStackLayoutManager.")
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        if (layoutManager == null) layoutManager = CardStackLayoutManager(context)

        adapter?.let {
            try {
                it.registerAdapterDataObserver(observer)
                super.setAdapter(it)
            } catch (e: Exception) {
                it.unregisterAdapterDataObserver(observer)

                observer = CardStackDataObserver(this)
                it.registerAdapterDataObserver(observer)
                super.setAdapter(it)
            }
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            logger("TAG", "onInterceptTouchEvent: =========")
            (layoutManager as? CardStackLayoutManager)?.updateProportion(event.x, event.y)
        }
        return super.onInterceptTouchEvent(event)
    }

    fun swipe() {
        (layoutManager as? CardStackLayoutManager)?.let { manager ->
            smoothScrollToPosition(manager.getTopPosition() + 1)
        }
    }

    fun rewind() {
        (layoutManager as? CardStackLayoutManager)?.let { manager ->
            smoothScrollToPosition(manager.getTopPosition() - 1)
        }
    }

    private fun initialize() {
        logger("TAG", "initialize: ====================")
        overScrollMode = OVER_SCROLL_NEVER
    }
}