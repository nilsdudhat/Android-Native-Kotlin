package com.belive.dating.helpers.helper_functions.event_management

import android.app.Activity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Base Event class for all events
 *
 * @since 2025-02-04 07:01:09 UTC
 */
class Event(
    val key: String,
    val value: Any?
)

/**
 * Enhanced Event Manager Implementation
 * Handles event distribution with lifecycle awareness
 *
 * Features:
 * 1. Event posting from anywhere
 * 2. Active subscriber filtering
 * 3. Off-screen event queueing
 * 4. Automatic cleanup
 * 5. No previous event delivery to new subscribers
 * 6. Automatic unsubscription
 * 7. Crash prevention
 * 8. Multi-event support
 * 9. No object creation requirement
 * 10. Global event listening
 * 11. Manual unsubscription
 * 12. Duplicate event prevention
 * 13. Subscriber tracking
 *
 * @since 2025-02-04 07:01:09 UTC
 */
object EventManager {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println("EventManager Error: ${throwable.message}")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main + exceptionHandler)
    private val eventFlow = MutableSharedFlow<EventData>(replay = 0, extraBufferCapacity = 20)
    private val pendingEvents = ConcurrentHashMap<String, ConcurrentLinkedQueue<EventData>>()
    private val subscribers = mutableSetOf<String>()
    private val subscriberStates = ConcurrentHashMap<String, SubscriberState>()
    private val subscriberCallbacks = ConcurrentHashMap<String, suspend (String, String, Any?) -> Unit>()
    private val lifecycleOwnerRefs = ConcurrentHashMap<String, WeakReference<LifecycleOwner>>()
    private val deliveredEvents = ConcurrentHashMap<String, MutableSet<String>>()

    private data class EventData(
        val id: String = "${System.nanoTime()}",
        val key: String,
        val value: Any?,
        val timestamp: Long = System.nanoTime()
    )

    private data class SubscriberState(
        var isLifecycleActive: Boolean = false,
        var isVisible: Boolean = false
    )

    /**
     * Posts an event to all subscribers
     */
    fun postEvent(event: Event) = scope.launch(exceptionHandler) {
        try {
            val eventData = EventData(key = event.key, value = event.value)

            subscribers.forEach { subscriberId ->
                val state = subscriberStates[subscriberId]
                when {
                    state?.isLifecycleActive == true && state.isVisible -> {
                        if (!isEventDelivered(subscriberId, eventData.id)) {
                            eventFlow.emit(eventData)
                        }
                    }

                    else -> {
                        if (!isEventDelivered(subscriberId, eventData.id)) {
                            pendingEvents.getOrPut(subscriberId) { ConcurrentLinkedQueue() }
                                .offer(eventData)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Post event error: ${e.message}")
        }
    }

    /**
     * Subscribe to events
     */
    fun listenToEvents(
        lifecycleOwner: LifecycleOwner,
        onEvent: suspend (String, String, Any?) -> Unit,
    ): String {
        val subscriberClassName = when (lifecycleOwner) {
            is Fragment -> lifecycleOwner.javaClass.simpleName
            else -> lifecycleOwner.javaClass.enclosingClass?.simpleName
                ?: lifecycleOwner.javaClass.simpleName
        }

        val subscriberId = buildSubscriberId(
            className = subscriberClassName,
            instanceHash = lifecycleOwner.hashCode(),
            timestamp = System.nanoTime(),
        )

        setupSubscriber(
            subscriberId = subscriberId,
            lifecycleOwner = lifecycleOwner,
            callback = onEvent
        )
        return subscriberId
    }

    private fun buildSubscriberId(
        className: String,
        instanceHash: Int,
        timestamp: Long,
    ): String = buildString {
        append(className)
        append("_")
        append(instanceHash)
        append("_")
        append(timestamp)
    }

    private fun setupSubscriber(
        subscriberId: String,
        lifecycleOwner: LifecycleOwner,
        callback: suspend (String, String, Any?) -> Unit,
    ) {
        try {
            lifecycleOwnerRefs[subscriberId] = WeakReference(lifecycleOwner)

            val observer = object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    updateSubscriberState(subscriberId)
                }

                override fun onResume(owner: LifecycleOwner) {
                    updateSubscriberState(subscriberId)
                }

                override fun onPause(owner: LifecycleOwner) {
                    updateSubscriberState(subscriberId)
                }

                override fun onStop(owner: LifecycleOwner) {
                    updateSubscriberState(subscriberId)
                }

                override fun onDestroy(owner: LifecycleOwner) {
                    unsubscribe(subscriberId)
                    lifecycleOwner.lifecycle.removeObserver(this)
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)
            subscriberCallbacks[subscriberId] = callback

            scope.launch(exceptionHandler) {
                eventFlow.collect { eventData ->
                    val state = subscriberStates[subscriberId]
                    if (state?.isLifecycleActive == true &&
                        state.isVisible &&
                        lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) &&
                        !isEventDelivered(subscriberId, eventData.id)
                    ) {
                        callback(eventData.key, subscriberId, eventData.value)
                        markEventDelivered(subscriberId, eventData.id)
                    }
                }
            }

            subscribers.add(subscriberId)
            subscriberStates[subscriberId] = SubscriberState(
                isLifecycleActive = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED),
                isVisible = checkVisibility(lifecycleOwner)
            )
        } catch (e: Exception) {
            println("Setup subscriber error: ${e.message}")
        }
    }

    private fun checkVisibility(lifecycleOwner: LifecycleOwner): Boolean {
        return when (lifecycleOwner) {
            is Fragment -> {
                lifecycleOwner.isResumed &&
                        lifecycleOwner.view?.isVisible == true &&
                        lifecycleOwner.userVisibleHint
            }

            is Activity -> {
                lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            }

            else -> {
                lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            }
        }
    }

    private fun updateSubscriberState(subscriberId: String) = scope.launch(exceptionHandler) {
        val lifecycleOwner = lifecycleOwnerRefs[subscriberId]?.get() ?: return@launch

        subscriberStates[subscriberId]?.let { state ->
            state.isLifecycleActive = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            state.isVisible = checkVisibility(lifecycleOwner)

            if (state.isLifecycleActive && state.isVisible) {
                processPendingEvents(subscriberId)
            }
        }
    }

    private fun isEventDelivered(subscriberId: String, eventId: String): Boolean {
        return deliveredEvents[subscriberId]?.contains(eventId) ?: false
    }

    private fun markEventDelivered(subscriberId: String, eventId: String) {
        deliveredEvents.getOrPut(subscriberId) { mutableSetOf() }.add(eventId)
    }

    private fun processPendingEvents(subscriberId: String) = scope.launch(exceptionHandler) {
        val callback = subscriberCallbacks[subscriberId] ?: return@launch
        pendingEvents[subscriberId]?.let { queue ->
            while (queue.isNotEmpty()) {
                queue.poll()?.let { eventData ->
                    if (!isEventDelivered(subscriberId, eventData.id)) {
                        callback(eventData.key, subscriberId, eventData.value)
                        markEventDelivered(subscriberId, eventData.id)
                    }
                }
            }
        }
    }

    /**
     * Manually unsubscribe from events
     */
    fun unsubscribe(subscriberId: String) {
        try {
            subscribers.remove(subscriberId)
            subscriberStates.remove(subscriberId)
            subscriberCallbacks.remove(subscriberId)
            pendingEvents.remove(subscriberId)
            lifecycleOwnerRefs.remove(subscriberId)
            deliveredEvents.remove(subscriberId)
        } catch (e: Exception) {
            println("Unsubscribe error: ${e.message}")
        }
    }

    /**
     * Clear all resources
     */
    fun clearAll() {
        subscribers.clear()
        pendingEvents.clear()
        subscriberStates.clear()
        subscriberCallbacks.clear()
        lifecycleOwnerRefs.clear()
        deliveredEvents.clear()
    }

    /**
     * Get subscriber info for debugging
     */
    fun getSubscriberInfo(subscriberId: String): String? {
        return try {
            val parts = subscriberId.split("_")
            if (parts.size >= 3) {
                val className = parts[0]
                val instanceHash = parts[1]
                val timestamp = parts[2]
                "Class: $className, Instance: $instanceHash, Created: $timestamp"
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}