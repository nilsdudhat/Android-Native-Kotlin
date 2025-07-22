package com.belive.dating.helpers.helper_functions.swipe_options

/**
 * Listener for monitoring events about swipe layout.
 */
interface SwipeListener {
    /**
     * Called when the main view becomes completely closed.
     */
    fun onClosed(view: SwipeRevealLayout?)

    /**
     * Called when the main view becomes completely opened.
     */
    fun onOpened(view: SwipeRevealLayout?, dragEdge: Int)

    /**
     * Called when the main view's position changes.
     *
     * @param slideOffset The new offset of the main view within its range, from 0-1
     */
    fun onSlide(view: SwipeRevealLayout?, slideOffset: Float)

    /**
     * Called when touch up down to this layout.
     *
     * @param isUp true is up. false is down.
     */
    fun onTouchUp(isUp: Boolean)
}