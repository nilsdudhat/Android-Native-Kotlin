package com.belive.dating.helpers.helper_functions.swipe_options

object MiscUtil {
    fun getStateString(state: Int): String {
        return when (state) {
            SwipeRevealLayout.STATE_CLOSE -> "state_close"
            SwipeRevealLayout.STATE_CLOSING -> "state_closing"
            SwipeRevealLayout.STATE_OPEN -> "state_open"
            SwipeRevealLayout.STATE_OPENING -> "state_opening"
            SwipeRevealLayout.STATE_DRAGGING -> "state_dragging"
            else -> "undefined"
        }
    }
}
