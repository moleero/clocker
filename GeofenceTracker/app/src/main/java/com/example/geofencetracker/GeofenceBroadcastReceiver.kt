package com.example.geofencetracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return

        if (event.hasError()) {
            Log.e("Geofence", "Błąd: ${GeofenceStatusCodes.getStatusCodeString(event.errorCode)}")
            return
        }

        val transition = event.geofenceTransition
        val triggeringGeofences = event.triggeringGeofences ?: return
        val now = System.currentTimeMillis()

        val db = AppDatabase.getInstance(context)

        CoroutineScope(Dispatchers.IO).launch {
            for (geofence in triggeringGeofences) {
                val zoneId = geofence.requestId.toLongOrNull() ?: continue
                val zone = db.zoneDao().getById(zoneId) ?: continue

                when (transition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        // unikamy duplikatu jesli juz jest otwarty wpis
                        if (db.logDao().getOpenEntry(zoneId) == null) {
                            db.logDao().insert(
                                LogEntry(zoneId = zoneId, zoneName = zone.name, enterTime = now)
                            )
                            notify(context, "Wejście: ${zone.name}")
                        }
                    }
                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        val open = db.logDao().getOpenEntry(zoneId)
                        if (open != null) {
                            open.exitTime = now
                            open.durationMillis = now - open.enterTime
                            db.logDao().update(open)
                            notify(context, "Wyjście: ${zone.name} (${formatDuration(open.durationMillis!!)})")
                        }
                    }
                }
            }
        }
    }

    private fun formatDuration(ms: Long): String {
        val totalMinutes = ms / 60000
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return "%dh %02dm".format(h, m)
    }

    private fun notify(context: Context, text: String) {
        val channelId = "geofence_events"
        val nm = NotificationManagerCompat.from(context)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("GeofenceTracker")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        try {
            nm.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            // brak uprawnienia POST_NOTIFICATIONS - ignorujemy, log dalej zapisany w bazie
        }
    }
}
