package com.demo.gallery.app.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.gallery.app.room.Media
import com.demo.gallery.app.room.MediaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MediaFactory(val applicationContext: Context)

class MediaViewModel(private val mediaFactory: MediaFactory) : ViewModel() {

    private val mediaRepository = MediaRepository(mediaFactory.applicationContext)

    fun getAllMedia(): LiveData<List<Media>> {
        return mediaRepository.getAllMedia()
    }

    fun insert(media: Media) {
        CoroutineScope(Dispatchers.IO).launch {
            mediaRepository.insert(media)
        }
    }

    fun update(media: Media) {
        CoroutineScope(Dispatchers.IO).launch {
            mediaRepository.update(media)
        }
    }

    fun delete(media: Media) {
        CoroutineScope(Dispatchers.IO).launch {
            mediaRepository.delete(media)
        }
    }

    fun deleteAllMedia() {
        CoroutineScope(Dispatchers.IO).launch {
            mediaRepository.deleteAllMedia()
        }
    }

    fun loadMediaByCursor(onFinished: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            mediaRepository.loadMediaByCursor(mediaFactory.applicationContext)
            onFinished(true)
        }
    }

    private suspend fun isMediaExistByPath(path: String): Boolean {
        return mediaRepository.isMediaExistByPath(path)
    }

    fun isMediaExistByPath(
        path: String,
        isExistCallback: (Boolean) -> Unit,
    ) {
        viewModelScope.launch {
            val isExist = withContext(Dispatchers.IO) {
                return@withContext mediaRepository.isMediaExistByPath(path)
            }
            isExistCallback(isExist)
        }
    }

    fun getMediaByPath(
        path: String,
        mediaCallback: (Media) -> Unit,
    ) {
        viewModelScope.launch {
            val media = withContext(Dispatchers.IO) {
                return@withContext mediaRepository.getMediaByPath(path)
            }
            mediaCallback(media)
        }
    }
}