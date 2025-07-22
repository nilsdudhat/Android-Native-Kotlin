package com.belive.dating.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.belive.dating.R
import com.belive.dating.api.user.UserClient
import com.belive.dating.api.user.UserService
import com.belive.dating.extensions.getBitmapFromUrl
import com.belive.dating.extensions.getGistPrefs
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        CoroutineScope(Dispatchers.IO).launch {
            if ((getGistPrefs().baseURL.isNotEmpty()) && getGistPrefs().baseURL.contains("http") && (getUserPrefs().userToken != null)) {
                val userClient = UserClient.getUserInstance()
                val userService = userClient.create(UserService::class.java)

                getUserPrefs().fcmToken = token

                val json = JsonObject()
                json.addProperty("fcm_token", token)

                val response = userService.updateUserDetails(json)
                val errorBody = response.errorBody()?.string()

                logger("--updateToken--", "code: " + response.code())
                logger("--updateToken--", "errorBody: $errorBody")
                logger("--updateToken--", "body: " + gsonString(response.body()))
                logger("--updateToken--", "url: " + response.raw().request.url)
                logger("--updateToken--", "isSuccessful: " + response.isSuccessful)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Handle the received message
        try {
            val jsonObject = JSONObject(gsonString(message))
            logger("--message--", "remoteMessage: ${gsonString(message)}")
            val link = jsonObject.getJSONObject("bundle").getJSONObject("mMap").getString("gcm.n.link")
            logger("--message--", "gcm.n.link: $link")

            message.data.let {
                val title = it["title"] ?: "New Notification"
                val body = it["body"] ?: "You have a new message"
                val userImage = it["image"]

                logger("--message--", gsonString(it))

                // Wake up the app
                val wakeLock =
                    (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Belive::WakeLockTag")
                wakeLock.acquire(3000)

                showPopupNotification(title, body, link, userImage)
            }
        } catch (e: Exception) {
            logger("--message--", "Exception: ${gsonString(e)}")
        }
    }

    private fun showPopupNotification(title: String, body: String, link: String, userImage: String?) {
        val uri = link.toUri()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.data = uri

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = "popup_notification_channel"

        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = if (userImage != null) {
                getBitmapFromUrl(getGistPrefs().imagesURL + userImage)
            } else null

            launch(Dispatchers.Main) {
                val notificationBuilder = NotificationCompat.Builder(this@MyFirebaseMessagingService, channelId)
                    .setSmallIcon(R.drawable.ic_notification) // Change to your notification icon
                    .setColor(ContextCompat.getColor(baseContext, R.color.colorPrimary))
                    .setColorized(true)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent) // Content-Screen Popup
                    .setDefaults(Notification.DEFAULT_ALL) // Enable sound, vibration
                    .apply {
                        if (bitmap != null) {
                            setLargeIcon(bitmap)
                        }
                    }

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId, "Popup Notifications",
                        NotificationManager.IMPORTANCE_HIGH,
                    ).apply {
                        description = "Channel for pop-up notifications"
                        enableLights(true)
                        enableVibration(true)
                        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    }
                    notificationManager.createNotificationChannel(channel)
                }

                // Show the notification
                if (ActivityCompat.checkSelfPermission(
                        this@MyFirebaseMessagingService, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                } else {
                    notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
                }
            }
        }
    }
}