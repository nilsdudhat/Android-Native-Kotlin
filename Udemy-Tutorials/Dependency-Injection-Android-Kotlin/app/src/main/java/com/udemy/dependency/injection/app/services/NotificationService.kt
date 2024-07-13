package com.udemy.dependency.injection.app.services

import android.util.Log
import com.udemy.dependency.injection.app.dagger.annotations.ActivityScope
import javax.inject.Inject
import javax.inject.Singleton

interface NotificationService {
    fun send(to: String, title: String, body: String)
}

class MessageService @Inject constructor(private val retryCount: Int) : NotificationService {
    override fun send(to: String, title: String, body: String) {
        Log.d("--notification--", "SMS Sent, tried for $retryCount times")
    }
}

/*
* Singleton is used to create only one object across app runtime life
* */
@ActivityScope
class EmailService @Inject constructor() : NotificationService {
    override fun send(to: String, title: String, body: String) {
        Log.d("--notification--", "Email Sent")
    }
}