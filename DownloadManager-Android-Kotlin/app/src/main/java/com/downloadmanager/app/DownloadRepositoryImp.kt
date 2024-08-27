package com.downloadmanager.app

import android.content.Context

class DownloadRepositoryImp(context: Context) : DownloadRepository {

    private val downloader = Downloader(context)

    override suspend fun downloadFile(url: String, fileName: String): Long {
        return downloader.downLoadFile(url, fileName)
    }
}