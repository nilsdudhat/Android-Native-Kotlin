package com.udemy.dependency.injection.app.services

import android.util.Log
import javax.inject.Inject
import javax.inject.Named

interface DatabaseService {
    fun saveUser(email: String, password: String)
}

class RoomDatabaseService @Inject constructor(@Named("firebase") val analyticsService: AnalyticsService) : DatabaseService {
    override fun saveUser(email: String, password: String) {
        Log.d("--user--", "User saved in Room Database")
        analyticsService.trackEvent("User Registration", "Successful Registration")
    }
}

class FirebaseDatabaseService @Inject constructor() : DatabaseService {
    override fun saveUser(email: String, password: String) {
        Log.d("--user--", "User saved in Firebase Database")
    }
}