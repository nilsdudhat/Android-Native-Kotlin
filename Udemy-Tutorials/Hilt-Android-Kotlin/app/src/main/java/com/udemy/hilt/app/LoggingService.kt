package com.udemy.hilt.app

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoggingService @Inject constructor() {

    fun log(message: String) {
        Log.d(this.toString(), message)
    }
}