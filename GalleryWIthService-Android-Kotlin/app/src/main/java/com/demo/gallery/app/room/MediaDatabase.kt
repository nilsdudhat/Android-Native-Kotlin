package com.demo.gallery.app.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Media::class], version = 1, exportSchema = false)
abstract class MediaDatabase : RoomDatabase() {

    abstract fun getMediaDao(): MediaDao

    companion object {
        @Volatile
        private var instance: MediaDatabase? = null

        fun getInstance(context: Context): MediaDatabase {
            return synchronized(this) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        MediaDatabase::class.java,
                        "media_table"
                    ).build()
                }
                return@synchronized instance!!
            }
        }
    }
}