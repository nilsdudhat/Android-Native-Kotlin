package com.belive.dating.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.belive.dating.R
import com.belive.dating.activities.chat.MediaSendType
import com.belive.dating.api.user.models.chat_file.FileDownloadModel
import com.belive.dating.constants.SocketConstants
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.fromJson
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.belive.dating.helpers.helper_functions.socket.SocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.concurrent.ConcurrentLinkedQueue

class FileDownloadService : Service() {
    private val CHANNEL_ID = "download_channel"
    private val NOTIF_ID = 4001

    private val queue = ConcurrentLinkedQueue<FileDownloadModel>()
    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + serviceJob)

    @Volatile
    private var isProcessing = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        restoreQueue()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.hasExtra("isStop") == true) {
            intent.getBooleanExtra("isStop", false).let {
                if (it) {
                    startForeground(
                        NOTIF_ID, NotificationCompat.Builder(this, CHANNEL_ID)
                            .setContentTitle("Stopping Downloads")
                            .setProgress(100, 0, true)
                            .setSmallIcon(android.R.drawable.stat_sys_download)
                            .setOnlyAlertOnce(true)
                            .setOngoing(true)
                            .build()
                    )
                    stopSelfSafely()
                    return START_NOT_STICKY
                }
            }
        }

        intent?.getStringExtra("modelDownload")?.fromJson<List<FileDownloadModel>?>()?.let {
            val existingUrls = queue.mapNotNull { it.file }.toSet()

            val newItems = intent.getStringExtra("modelDownload")
                ?.fromJson<List<FileDownloadModel>>()
                ?.filter { it.file !in existingUrls }
                ?: emptyList()

            queue.addAll(newItems)

            logger("--download--", queue.size)

            if (queue.size > 100) {
                queue.clear()
                stopSelf()
                return START_NOT_STICKY
            }

            persistQueue()
        }

        if (!isProcessing && queue.isNotEmpty()) {
            isProcessing = true

            startForeground(
                NOTIF_ID, NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Preparing to Download")
                    .setProgress(100, 0, true)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .build()
            )

            scope.launch { processQueue() }
        }

        return START_STICKY
    }

    private fun processQueue() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading Media")
            .setProgress(100, 0, true)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOnlyAlertOnce(true)
            .setOngoing(true)

        while (queue.isNotEmpty()) {
            if (getUserPrefs().userToken.isNullOrEmpty()) {
                stopSelfSafely()
                break
            }

            val item = queue.poll() ?: continue
            if (item.file == null) continue

            try {
//                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val appDir = File(cacheDir, getString(R.string.app_name))

                if (!appDir.exists()) {
                    appDir.mkdirs()
                }

                val nameParts = item.mediaName?.split(".") ?: listOf("downloaded_file", "bin")
                val baseName = nameParts[0]
                val extension = nameParts.getOrElse(1) { "bin" }

                val uniqueFileName = createUniqueFile(appDir, baseName, extension)
                val nFile = File(appDir, uniqueFileName)

                if (nFile.exists()) {
                    sendProgressUpdate(100, nFile.absolutePath, item.msgId, true)

                    val obj = JSONObject().apply {
                        put("message_id", item.msgId)
                        put("receiver_media_path", nFile.absolutePath)
                        put("receiver_id", item.friendId)
                        put("sender_id", item.userId)
                    }
                    SocketManager.emit(SocketConstants.ON_MEDIA_PATH_UPDATE, obj)
                    continue
                }

                if (nFile.isDirectory) {
                    nFile.deleteRecursively()
                }

                val connection = URL(item.file).openConnection()
                connection.connect()
                val fileLength = connection.contentLength
                val input = BufferedInputStream(connection.getInputStream())
                val output = FileOutputStream(nFile)

                val data = ByteArray(1024)
                var total: Long = 0
                var count: Int

                while (input.read(data).also { count = it } != -1) {
                    if (getUserPrefs().userToken.isNullOrEmpty()) {
                        break
                    }

                    total += count
                    val progress = (total * 100 / fileLength).toInt()
                    sendProgressUpdate(progress, item.file, item.msgId, false)

                    notificationManager.notify(
                        NOTIF_ID, builder
                            .setContentTitle("Downloading ${if (item.mediaType == MediaSendType.IMAGE.value) "Image" else "Video"}: ${item.mediaName}")
                            .setProgress(100, progress, false)
                            .build()
                    )
                    output.write(data, 0, count)
                }

                output.flush()
                output.close()
                input.close()

                sendProgressUpdate(100, nFile.absolutePath, item.msgId, true)

                val obj = JSONObject().apply {
                    put("message_id", item.msgId)
                    put("receiver_media_path", nFile.absolutePath)
                    put("receiver_id", item.friendId)
                    put("sender_id", item.userId)
                }
                SocketManager.emit(SocketConstants.ON_MEDIA_PATH_UPDATE, obj)

                if (getUserPrefs().userToken.isNullOrEmpty()) {
                    stopSelfSafely()
                    break
                }
            } catch (e: Exception) {
                catchLog("--download-- error: ${gsonString(e)}")
                sendProgressUpdate(-1, "", item.msgId, false)
                continue
            }

            persistQueue()
        }

        stopSelfSafely()
    }

    private fun createUniqueFile(
        directory: File,
        baseName: String,
        extension: String,
    ): String {
        var count = 0
        var fileName = "$baseName.$extension"
        var file = File(directory, fileName)

        while (file.exists()) {
            count++
            fileName = "${baseName}_$count.$extension"
            file = File(directory, fileName)
        }

        return fileName
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(CHANNEL_ID, "Download Media", NotificationManager.IMPORTANCE_LOW)
                .also { getSystemService(NotificationManager::class.java).createNotificationChannel(it) }
        }
    }

    private fun sendProgressUpdate(progress: Int, filePath: String, messageId: Int, isSuccess: Boolean = false) {
        val intent = Intent("FILE_DOWNLOAD_PROGRESS")
        intent.putExtra("index", messageId)
        intent.putExtra("progress", progress)
        intent.putExtra("file", filePath)
        intent.putExtra("isSuccess", isSuccess)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun persistQueue() {
        val newList: ArrayList<FileDownloadModel>? = arrayListOf<FileDownloadModel>()

        if (getUserPrefs().runningFileDownloadList != null) {
            newList?.addAll(getUserPrefs().runningFileDownloadList!!)
        }

        if (queue.isNotEmpty()) {
            val existingUrls = queue.mapNotNull { it.file }.toSet()

            val newItems = queue.filter { it.file !in existingUrls }

            queue.addAll(newItems)
        }

        newList?.addAll(queue)

        newList?.distinctBy { it.msgId }?.let { distFiles ->
            getUserPrefs().runningFileDownloadList = ArrayList(distFiles)
        }
    }

    private fun restoreQueue() {
        queue.clear()
        getUserPrefs().runningFileDownloadList?.distinctBy { it.msgId }?.let {
            queue.addAll(it)
        }
    }

    private fun stopSelfSafely() {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIF_ID)
        isProcessing = false
        getUserPrefs().runningFileDownloadList = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

fun Context.stopDownloadService() {
    Intent(this, FileDownloadService::class.java).apply {
        putExtra("isStop", true)
        startService(this)

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(this)
        else
            startService(this)*/
    }
}

fun Context.enqueueDownload(list: ArrayList<FileDownloadModel>) {
    Intent(this, FileDownloadService::class.java).apply {
        putExtra("modelDownload", gsonString(list))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(this)
        else
            startService(this)
    }
}