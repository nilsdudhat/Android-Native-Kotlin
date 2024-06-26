package com.udemy.service.app

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.provider.Settings

/*
* A service in android is a component that runs in the background and
* dose not require a user interaction to run.
* */
class MyService: Service() {

    lateinit var mediaPlayer: MediaPlayer

    override fun onBind(p0: Intent?): IBinder? {
        // return null: if binding is not allowed
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI)
        mediaPlayer.isLooping = true
        mediaPlayer.start()


        // return value: returns an integer that represents the service's behaviour regarding restarts

        // START_STICKY: tells the system to create a fresh copy of the service,
        //               when sufficient memory is available, after it recovers from low memory.
        //               Here you will lose the results that might have computed before.
        // START_NOT_STICKY: tells the system not to bother to restart the service,
        //                   even when it has sufficient memory.
        // START_REDELIVER_INTENT: tells the system to restart the service after the terminated and
        //                         also redeliver the intents that were present at the time of termination.

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        super.onDestroy()
    }
}