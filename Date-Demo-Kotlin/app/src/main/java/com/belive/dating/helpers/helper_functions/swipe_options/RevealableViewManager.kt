package com.belive.dating.helpers.helper_functions.swipe_options

import android.graphics.Rect
import android.view.View

class RevealableViewManager {
    private val list = HashSet<RevealableViewModel>()

    private val edgeMap = HashMap<Int?, RevealableViewModel?>()
    private val viewMap = HashMap<View?, RevealableViewModel?>()

    fun putRevealableView(group: RevealableViewModel?) {
        list.add(group!!)
        generateDataSet()
    }

    fun getGroupFromView(view: View?): RevealableViewModel? {
        return viewMap[view]
    }

    fun getGroupFromEdge(edge: Int): RevealableViewModel? {
        return edgeMap.get(edge)
    }

    fun getMainOpenRect(main: Rect, edge: Int): Rect {
        val mRectMainOpen = Rect()
        val view = getGroupFromEdge(edge)?.view
        if (view != null)
            when (edge) {
                SwipeRevealLayout.DRAG_EDGE_LEFT -> mRectMainOpen.set(main.left + view.width, main.top, main.right + view.width, main.bottom)
                SwipeRevealLayout.DRAG_EDGE_RIGHT -> mRectMainOpen.set(main.left - view.width, main.top, main.right - view.width, main.bottom)
                SwipeRevealLayout.DRAG_EDGE_TOP -> mRectMainOpen.set(main.left, main.top + view.height, main.right, main.bottom + view.height)
                SwipeRevealLayout.DRAG_EDGE_BOTTOM -> mRectMainOpen.set(
                    main.left,
                    main.top - view.height,
                    main.right,
                    main.bottom - view.height
                )
            }
        return mRectMainOpen
    }

    fun offsetLeftAndRight(dx: Int) {
        for (model in list) {
            model.view.offsetLeftAndRight(dx)
        }
    }

    fun offsetTopAndBottom(dy: Int) {
        for (model in list) {
            model.view.offsetTopAndBottom(dy)
        }
    }

    private fun generateDataSet() {
        edgeMap.clear()
        viewMap.clear()
        for (group in list) {
            edgeMap.put(group.dragEdge, group)
            viewMap.put(group.view, group)
        }
    }
}