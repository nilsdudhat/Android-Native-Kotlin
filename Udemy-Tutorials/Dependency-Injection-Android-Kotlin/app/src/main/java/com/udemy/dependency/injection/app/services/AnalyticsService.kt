package com.udemy.dependency.injection.app.services

import android.util.Log
import javax.inject.Inject

interface AnalyticsService {
    fun trackEvent(eventName: String, eventType: String)
}

class Mixpanel @Inject constructor() : AnalyticsService {
    override fun trackEvent(eventName: String, eventType: String) {
        Log.d("--track--", "Mixpanel: $eventName: $eventType")
    }
}

class FirebaseAnalytics @Inject constructor() : AnalyticsService {
    override fun trackEvent(eventName: String, eventType: String) {
        Log.d("--track--", "FirebaseAnalytics: $eventName: $eventType")
    }
}