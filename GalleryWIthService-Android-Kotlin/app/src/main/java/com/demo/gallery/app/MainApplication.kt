package com.demo.gallery.app

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.demo.gallery.app.room.MediaDatabase
import com.demo.gallery.app.room.MediaRepository

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        getDatabase()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    fun getDatabase(): MediaDatabase {
        return MediaDatabase.getInstance(this)
    }

    fun getMediaRepository(): MediaRepository {
        return MediaRepository(this)
    }
}