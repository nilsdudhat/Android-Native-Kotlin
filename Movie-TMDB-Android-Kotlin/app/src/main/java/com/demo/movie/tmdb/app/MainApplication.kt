package com.demo.movie.tmdb.app

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.demo.movie.tmdb.app.api.APIClient
import com.demo.movie.tmdb.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            modules(listOf(appModule))
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}