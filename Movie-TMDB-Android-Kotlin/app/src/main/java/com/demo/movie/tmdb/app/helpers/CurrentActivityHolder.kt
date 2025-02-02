package com.demo.movie.tmdb.app.helpers

import android.app.Activity
import android.app.Application
import android.os.Bundle

class CurrentActivityHolder : Application.ActivityLifecycleCallbacks {
    private var currentActivity: Activity? = null

    fun getCurrentActivity(): Activity? = currentActivity

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}