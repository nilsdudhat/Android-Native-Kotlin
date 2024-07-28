package com.demo.movie.tmdb.app

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.demo.movie.tmdb.app.api.APIClient

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        APIClient.getInstance()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}