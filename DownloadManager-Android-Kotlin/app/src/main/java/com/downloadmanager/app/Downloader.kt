package com.downloadmanager.app

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap

class Downloader(context: Context) {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    fun downLoadFile(url: String, fileName: String): Long {
        var fileExtension: String? = null
        var mimeType: String? = null
        try {
            fileExtension = url.substringAfterLast(".")
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)
        } catch (e: Exception) {
            Log.d("--catch--", "downLoadFile: $e")
        }

        val request = DownloadManager.Request(Uri.parse(url))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setMimeType(mimeType)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "$fileName${if (fileExtension != null) ".$fileExtension" else ""}",
            )

        return downloadManager.enqueue(request)
    }
}