package com.belive.dating.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.graphics.BitmapFactory
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.belive.dating.R
import com.belive.dating.activities.dashboard.main.MainActivity
import com.belive.dating.extensions.formatTimeForBoost
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.isAppInForeground
import com.belive.dating.extensions.logger
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BoostService : Service() {
    private val notificationId = 1
    private val channelId = "boost_channel_id"
    private val countDownInterval: Long = 1000 // Timer interval in milliseconds
    private var countDownTimer: CountDownTimer? = null
    private val receiver = BoostBroadcastReceiver()

    class BoostBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "countdown_tick") {
                val timeLeft = intent.getLongExtra("time_left", 0)
                val isFinish = intent.getBooleanExtra("finish", false)
                // Handle the data received

                context?.let { getUserPrefs().isBoostRunning = isFinish }
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val filter = IntentFilter("countdown_tick")

        // Register receiver with compatibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }

        // Start foreground service with compatibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                notificationId,
                createNotification(),
                FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(notificationId, createNotification())
        }

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        val calendar: Calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
        val formattedTimeStart: String = sdf.format(calendar.time)
        getUserPrefs().boostStartTime = formattedTimeStart

        val calendarE = Calendar.getInstance()
        calendarE.add(Calendar.MINUTE, (getUserPrefs().boostTime.toLong() / (1000 * 60)).toInt()) // Add 10 minutes to the current time
        val formattedTimeEnd: String = sdf.format(calendarE.time)
        getUserPrefs().boostEndTime = formattedTimeEnd

        startTimer()
    }

    private fun startTimer() {
        updateNotification(getUserPrefs().boostTime.toLong())
        updateUI(getUserPrefs().boostTime.toLong(), true)

        countDownTimer = object : CountDownTimer(getUserPrefs().boostTime.toLong(), countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                updateNotification(millisUntilFinished)
                updateUI(millisUntilFinished, true)
            }

            override fun onFinish() {
                updateUI(0, false)
                stopSelf() // Stop the service when the timer finishes
            }
        }.start()
    }

    private fun updateNotification(millisUntilFinished: Long) {
        logger("--notification--", "onTick: $millisUntilFinished")

        val timeFormatted = formatTimeForBoost(millisUntilFinished)

        val notification: Notification = if (isAppInForeground()) {
            logger("--notification--", "isAppInForeground: true")

            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_small_notification)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_small_notification))
                .setContentTitle("Boost : $timeFormatted")
                .setSilent(true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        } else {
            logger("--notification--", "appInForeground: false")

            val notificationIntent = Intent(this, MainActivity::class.java)
            notificationIntent.putExtra("display_splash", false)

            val pendingIntent =
                PendingIntent.getActivity(
                    this,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_small_notification)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_small_notification))
                .setContentTitle("Boost : $timeFormatted")
                .setSilent(true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build()
        }

        val manager = getSystemService(NotificationManager::class.java)

        manager.notify(notificationId, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Boost",
            NotificationManager.IMPORTANCE_HIGH,
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_small_notification)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_small_notification))
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText("Boost is running...")
            .setSilent(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (!isAppInForeground()) {
            val notificationIntent = Intent(this, MainActivity::class.java)
            notificationIntent.putExtra("display_splash", false)

            val pendingIntent =
                PendingIntent.getActivity(
                    this,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            builder.setContentIntent(pendingIntent)
        }

        return builder.build()
    }

    private fun updateUI(millisUntilFinished: Long, state: Boolean) {
        val intent = Intent("countdown_tick")
        intent.putExtra("time_left", millisUntilFinished)
        intent.putExtra("finish", state)
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        stopForeground(true)
        stopSelf()
        countDownTimer?.cancel()
        super.onDestroy()
    }
}