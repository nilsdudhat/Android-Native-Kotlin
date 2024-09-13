package com.eventbus.app

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class MyService : Service() {

    private val appEventBus by inject<AppEventBus>()
    private val scope = MainScope() + CoroutineName("MyService")

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            // Send event with data
            appEventBus.emit(AppEvent.MyEventWithData(message = "Hello from Service!"))

            // Send event without data for performing any action
            appEventBus.emit(AppEvent.MyEventWithoutData)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}