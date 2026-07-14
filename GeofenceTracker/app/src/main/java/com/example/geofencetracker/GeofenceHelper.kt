package com.example.geofencetracker

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceHelper(private val context: Context) {

    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        intent.action = "com.example.geofencetracker.ACTION_GEOFENCE_EVENT"
        PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    fun registerZone(zone: Zone, onComplete: (Boolean) -> Unit) {
        val geofence = Geofence.Builder()
            .setRequestId(zone.id.toString())
            .setCircularRegion(zone.latitude, zone.longitude, zone.radiusMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setLoiteringDelay(30_000) // 30s zanim uzna ze faktycznie tam jestesmy
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(request, geofencePendingIntent)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun registerAllZones(zones: List<Zone>, onComplete: (Boolean) -> Unit) {
        if (zones.isEmpty()) { onComplete(true); return }
        var remaining = zones.size
        var allOk = true
        zones.forEach { zone ->
            registerZone(zone) { ok ->
                if (!ok) allOk = false
                remaining--
                if (remaining == 0) onComplete(allOk)
            }
        }
    }

    fun removeZone(zoneId: Long) {
        geofencingClient.removeGeofences(listOf(zoneId.toString()))
    }
}
