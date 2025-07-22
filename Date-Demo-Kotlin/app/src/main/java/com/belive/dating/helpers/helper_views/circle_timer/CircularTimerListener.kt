package com.belive.dating.helpers.helper_views.circle_timer

interface CircularTimerListener {
    fun updateDataOnTick(remainingTimeInMs: Long): String?
    fun onTimerFinished()
}