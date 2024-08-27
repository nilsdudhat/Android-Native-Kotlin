package com.downloadmanager.app

import android.app.Application
import android.app.DownloadManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DownloadViewModel(application: Application) : AndroidViewModel(application) {

    private val downloadRepository = DownloadRepositoryImp(application)
    private val downloadManager = application.getSystemService(DownloadManager::class.java)

    // used to track the progress e.g horizontal loading bar
    private val _progress = MutableLiveData<Double>()
    val progress: LiveData<Double>
        get() = _progress

    // just for teaching purpose let's display the current downloadId

    private val _downloadId = MutableLiveData<Long>()
    val downloadId: LiveData<Long>
        get() = _downloadId

    fun startDownload(url: String, fileName: String) = viewModelScope.launch(Dispatchers.IO) {
        val downloadManager = downloadRepository.downloadFile(url, fileName)
        _downloadId.postValue(downloadManager)
        observeProgress(downloadManager)
    }

    private fun observeProgress(downloadId: Long) {
        val query = DownloadManager.Query().setFilterById(downloadId)
        var downloading = true

        while (downloading) {
            val cursor = downloadManager.query(query)
            cursor?.use {
                if (it.moveToFirst()) {
                    val statusIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val progressIndex = it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalIndex = it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                    val status = if (statusIndex != -1) it.getInt(statusIndex) else DownloadManager.STATUS_FAILED
                    val progressValue = if (progressIndex != -1) it.getDouble(progressIndex) else 0
                    val total = if (totalIndex != -1) it.getDouble(totalIndex) else 0

                    if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                        downloading = false
                    }

                    if (total != 0) {
                        val downloadProgress = ((progressValue as Double) / (total as Double)) * 100

                        Log.d("--download--", "observeProgress: $total, $progressValue : $downloadProgress")

                        _progress.postValue(downloadProgress)
                    }
                }
            }
            cursor.close()

            Thread.sleep(1000) // Wait for 1 second before querying again
        }
    }
}