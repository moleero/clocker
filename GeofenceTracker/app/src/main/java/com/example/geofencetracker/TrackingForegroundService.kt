package com.example.geofencetracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class TrackingForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GeofenceTracker aktywny")
            .setContentText("Śledzenie stref w tle")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

        startForeground(NOTIF_ID, notification)
        return START_STICKY
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Śledzenie w tle", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

            val eventsChannel = NotificationChannel(
                "geofence_events", "Zdarzenia stref", NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(eventsChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "tracking_service"
        const val NOTIF_ID = 1
    }
}
