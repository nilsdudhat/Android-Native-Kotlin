package com.belive.dating.helpers.helper_functions.double_click

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View

abstract class DoubleClickListener : View.OnClickListener {
    companion object {
        private const val DEFAULT_QUALIFICATION_SPAN: Long = 200
    }

    private var isSingleEvent: Boolean = false
    private var doubleClickQualificationSpanInMillis: Long = DEFAULT_QUALIFICATION_SPAN
    private var timestampLastClick: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
        if (isSingleEvent) {
            onSingleClick()
        }
    }

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - timestampLastClick < doubleClickQualificationSpanInMillis) {
            isSingleEvent = false
            handler.removeCallbacks(runnable)
            onDoubleClick()
            return
        }

        isSingleEvent = true
        handler.postDelayed(runnable, DEFAULT_QUALIFICATION_SPAN)
        timestampLastClick = SystemClock.elapsedRealtime()
    }

    abstract fun onDoubleClick()
    abstract fun onSingleClick()
}