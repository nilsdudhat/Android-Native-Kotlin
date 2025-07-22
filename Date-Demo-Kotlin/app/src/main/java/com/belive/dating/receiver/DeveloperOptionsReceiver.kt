package com.belive.dating.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings

interface DeveloperOptionsCallback {
    fun onDeveloperOptionsChanged(isAdbEnabled: Boolean)
}

class DeveloperOptionsReceiver(private val callback: DeveloperOptionsCallback) :
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_USER_PRESENT, Intent.ACTION_CONFIGURATION_CHANGED -> {
                // Check if USB debugging is enabled
                val isAdbEnabled = isUsbDebuggingEnabled(context)
                callback.onDeveloperOptionsChanged(isAdbEnabled)
            }
        }
    }

    private fun isUsbDebuggingEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED) == 1
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }
}