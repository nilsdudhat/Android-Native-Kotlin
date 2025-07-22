package com.belive.dating.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.belive.dating.activities.chat.MediaSendType
import com.belive.dating.api.user.models.chat_file.FileUploadModel
import com.belive.dating.constants.SocketConstants
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.fromJson
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.belive.dating.helpers.helper_functions.socket.SocketManager
import com.belive.dating.helpers.helper_functions.tus_upload.TusPreferencesURLStore
import io.tus.java.client.TusClient
import io.tus.java.client.TusExecutor
import io.tus.java.client.TusUpload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.util.concurrent.ConcurrentLinkedQueue

class FileUploadService : Service() {

    private val CHANNEL_ID = "upload_channel"
    private val NOTIF_ID = 3001

    private val queue = ConcurrentLinkedQueue<FileUploadModel>()
    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var client: TusClient? = null

    @Volatile
    private var isProcessing = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initTusClient()
        restoreQueue() // repopulate queue if service restarted by OS
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.hasExtra("isStop") == true) {
            intent.getBooleanExtra("isStop", false).let {
                if (it) {
                    startForeground(
                        NOTIF_ID,
                        NotificationCompat.Builder(this, CHANNEL_ID)
                            .setContentTitle("Stopping Uploads")
                            .setProgress(100, 0, true)
                            .setSmallIcon(android.R.drawable.stat_sys_upload)
                            .setOnlyAlertOnce(true)
                            .setOngoing(true)
                            .build()
                    )
                    stopSelfSafely()
                    return START_NOT_STICKY
                }
            }
        }

        intent?.getStringExtra("modelUpload")?.fromJson<List<FileUploadModel>?>()?.let {
            val currentPaths = queue.mapNotNull { it.file }.toSet()
            it.filter { model -> model.file !in currentPaths }.forEach { model -> queue.add(model) }

            persistQueue()
        }

        if (!isProcessing && queue.isNotEmpty()) {
            isProcessing = true
            startForeground(
                NOTIF_ID,
                NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Preparing to Upload")
                    .setProgress(100, 0, true)
                    .setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .build()
            )
            scope.launch { processQueue() }
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(CHANNEL_ID, "Upload Media", NotificationManager.IMPORTANCE_LOW)
                .also { getSystemService(NotificationManager::class.java).createNotificationChannel(it) }
        }
    }

    private fun initTusClient() {
        val pref = getSharedPreferences("tus", 0)
        client = TusClient()
        client?.uploadCreationURL = URL(getGistPrefs().baseURL + "file-upload/")
        client?.enableResuming(TusPreferencesURLStore(pref))
        client!!.enableRemoveFingerprintOnSuccess()
        client!!.removeFingerprintOnSuccessEnabled()
        pref.edit {
            clear()
        }
    }

    private fun processQueue() {
        try {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Uploading Media")
                .setProgress(100, 0, true)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setOnlyAlertOnce(true)
                .setOngoing(true)

            notificationManager.notify(NOTIF_ID, builder.build())

            while (queue.isNotEmpty()) {
                if (getUserPrefs().userToken.isNullOrEmpty()) {
                    stopSelfSafely()
                    break
                }
                if (client == null) break
                val item = queue.poll() ?: break
                if (item.file == null) continue

                val file = File(item.file)

                logger("--upload--", "file: ${item.file}")

                notificationManager.notify(
                    NOTIF_ID,
                    builder
                        .setContentTitle("Uploading ${if (item.mediaType == MediaSendType.IMAGE.value) "Image" else "Video"}: ${file.name}")
                        .setProgress(100, 0, true)
                        .build()
                )

                val upload = TusUpload(file)

                // Wrap upload logic in TusExecutor
                object : TusExecutor() {
                    override fun makeAttempt() {
                        val uploader = client!!.resumeOrCreateUpload(upload)
                        uploader.chunkSize = 1024
                        do {
                            if (getUserPrefs().userToken.isNullOrEmpty()) {
                                break
                            }

                            val uploaded = uploader.offset
                            val total = upload.size
                            val pct = (uploaded * 100 / total).toInt()

                            logger("--upload--", "file: ${item.file}, progress: $pct")

                            notificationManager.notify(
                                NOTIF_ID,
                                builder
                                    .setContentTitle("Uploading ${if (item.mediaType == MediaSendType.IMAGE.value) "Image" else "Video"}: ${file.name}")
                                    .setProgress(100, pct, false)
                                    .build()
                            )

                            sendProgressUpdate(pct, item)

                        } while (uploader.uploadChunk() > -1)

                        uploader.finish()    // finalize upload
                        logger("--upload--", "file: ${item.file} done")

                        item.file.let { File(it).length() }.let { size ->
                            val obj = JSONObject()
                            obj.put("message_id", item.msgId)
                            obj.put("media_key", uploader.uploadURL.path.substringAfterLast('/'))
                            obj.put("media_name", upload.metadata["filename"])
                            obj.put("media_size", size)
                            obj.put("media_type", item.mediaType)
                            obj.put("sender_id", getUserPrefs().userId)
                            obj.put("receiver_id", item.friendId)

                            logger("--media--", gsonString(obj))

                            SocketManager.emit(SocketConstants.ON_MEDIA_SEND, obj)

                            sendProgressUpdate(100, item, true)
                        }
                    }
                }.makeAttempts()

                if (getUserPrefs().userToken.isNullOrEmpty()) {
                    stopSelfSafely()
                    break
                }

                persistQueue()
            }

            logger("--upload--", "queue is done")

            stopSelfSafely()
        } catch (e: Exception) {
            catchLog("processQueue: ${gsonString(e)}")

            stopSelfSafely()
        }
    }

    private fun stopSelfSafely() {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIF_ID)
        getUserPrefs().runningFileUploadList = null
        isProcessing = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        stopSelf()
    }

    private fun sendProgressUpdate(progress: Int, fileUploadModel: FileUploadModel, isSuccess: Boolean = false) {
        logger("--upload--", "sending progress: $progress")

        val intent = Intent("FILE_UPLOAD_PROGRESS")
        intent.putExtra("index", fileUploadModel.msgId)
        intent.putExtra("progress", progress)
        intent.putExtra("file", fileUploadModel.file)
        intent.putExtra("isSuccess", isSuccess)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun persistQueue() {
        val newList: ArrayList<FileUploadModel>? = arrayListOf<FileUploadModel>()

        if (getUserPrefs().runningFileUploadList != null) {
            newList?.addAll(getUserPrefs().runningFileUploadList!!)
        }

        if (queue.isNotEmpty()) {
            newList?.addAll(queue.toList())
        }

        newList?.distinctBy { it.msgId }?.let {
            getUserPrefs().runningFileUploadList = ArrayList(it)
        }
    }

    private fun restoreQueue() {
        queue.clear()
        getUserPrefs().runningFileUploadList?.let { queue.addAll(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

fun Context.stopUploadService() {
    Intent(this, FileUploadService::class.java).apply {
        putExtra("isStop", true)
        startService(this)

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(this)
        else
            startService(this)*/
    }
}

fun Context.enqueueUpload(list: ArrayList<FileUploadModel>) {
    Intent(this, FileUploadService::class.java).apply {
        putExtra("modelUpload", gsonString(list))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(this)
        else
            startService(this)
    }
}
