package com.udemy.service.broadcastreceiver.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AirplaneModeReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if ((intent != null) && (intent.action != null) && (intent.action == Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            val isAirplaneModeEnabled = intent.getBooleanExtra("state", false)

            if (isAirplaneModeEnabled) {
                Toast.makeText(context, "Airplane Mode is Enabled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Airplane Mode is Disabled", Toast.LENGTH_LONG).show()
            }
        }
    }
}