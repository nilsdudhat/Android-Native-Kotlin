package com.demo.gallery.app.room

import android.content.Context
import androidx.lifecycle.LiveData

class MediaRepository(context: Context) {

    private val mediaDao: MediaDao

    init {
        val mediaDatabase = MediaDatabase.getInstance(context)
        mediaDao = mediaDatabase.getMediaDao()
    }

    fun getAllMedia(): LiveData<List<Media>> {
        return mediaDao.getAllMedia()
    }

    suspend fun insert(media: Media) {
        mediaDao.insert(media)
    }

    suspend fun update(media: Media) {
        mediaDao.update(media)
    }

    suspend fun delete(media: Media) {
        mediaDao.delete(media)
    }

    suspend fun deleteAllMedia() {
        mediaDao.deleteAllMedia()
    }

    suspend fun isMediaExistByPath(path: String): Boolean {
        return mediaDao.isMediaExistByPath(path)
    }

    suspend fun getMediaByPath(path: String): Media {
        return mediaDao.getMediaByPath(path)
    }
}