package com.eventbus.app

sealed class AppEvent {
    data class MyEventWithData(val message: String) : AppEvent()
    data object MyEventWithoutData : AppEvent()
}