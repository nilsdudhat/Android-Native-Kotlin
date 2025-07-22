package com.belive.dating.activities

import androidx.fragment.app.Fragment
import com.belive.dating.helpers.helper_functions.event_management.EventManager

open class EventBusFragment : Fragment() {

    private var eventId: String? = null

    fun listenEvents() {
        // Listen to all events
        eventId = EventManager.listenToEvents(this) { key, subscriberId, value ->
            observeEvents(key, subscriberId, value)
        }
    }

    override fun onDestroy() {
        eventId?.let { EventManager.unsubscribe(it) }
        super.onDestroy()
    }

    open fun observeEvents(key: String, subscriberId: String, value: Any?) {

    }
}