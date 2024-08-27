package com.downloadmanager.app

interface DownloadRepository {
    suspend fun downloadFile(url: String, fileName: String): Long
}