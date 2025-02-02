package com.demo.movie.tmdb.app

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.demo.movie.tmdb.app.di.appModule
import com.demo.movie.tmdb.app.helpers.CurrentActivityHolder
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            modules(listOf(appModule))
        }

        // Register CurrentActivityHolder
        val activityHolder: CurrentActivityHolder = getKoin().get()
        registerActivityLifecycleCallbacks(activityHolder)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}