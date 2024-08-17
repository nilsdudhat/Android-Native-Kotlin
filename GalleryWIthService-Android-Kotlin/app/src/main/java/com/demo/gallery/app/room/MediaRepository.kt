package com.demo.gallery.app.room

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(context: Context) {

    private val mediaDao: MediaDao

    init {
        val mediaDatabase = MediaDatabase.getInstance(context)
        mediaDao = mediaDatabase.getMediaDao()
    }

    suspend fun loadMediaByCursor(applicationContext: Context): Boolean {
        val list = withContext(Dispatchers.IO) {
            return@withContext com.demo.gallery.app.utils.getAllMedia(applicationContext)
        }

        for (model in list) {
            if (!isMediaExistByPath(model.path)) {
                insert(model)
            }
        }
        return true
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