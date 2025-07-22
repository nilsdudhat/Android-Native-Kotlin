package com.belive.dating.helpers.helper_functions.swipe_options

import android.graphics.Rect
import android.view.View

class RevealableViewModel(
    @JvmField val view: View, // getter setter
    var dragEdge: Int
) {
    init {
        this.dragEdge = dragEdge
    }

    fun layoutClose() {
        view.layout(
            this.closeRect.left,
            this.closeRect.top,
            this.closeRect.right,
            this.closeRect.bottom,
        )
    }

    fun layoutOpen() {
        view.layout(
            this.openRect.left,
            this.openRect.top,
            this.openRect.right,
            this.openRect.bottom
        )
    }

    val width: Int
        get() = view.width

    val height: Int
        get() = view.height

    private val closeRect: Rect
        //dynamic generate view rect to avoid view hasn't initialized size yet.
        get() = Rect(
            view.left,
            view.top,
            view.right,
            view.bottom
        )

    private val openRect: Rect
        get() = Rect(
            this.secOpenLeft,
            this.secOpenTop,
            this.secOpenLeft + view.width,
            this.secOpenTop + view.height
        )

    private val secOpenLeft: Int
        get() {
            return if (this.dragEdge == SwipeRevealLayout.DRAG_EDGE_LEFT) {
                view.left + view.width
            } else {
                view.left - view.width
            }
        }

    private val secOpenTop: Int
        get() = view.top
}
