package com.example.geofencetracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val db = AppDatabase.getInstance(context)
        val helper = GeofenceHelper(context)

        CoroutineScope(Dispatchers.IO).launch {
            val zones = db.zoneDao().getAll().first()
            helper.registerAllZones(zones) { }
        }
    }
}
