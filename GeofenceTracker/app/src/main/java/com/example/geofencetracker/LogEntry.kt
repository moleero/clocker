package com.example.geofencetracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log_entries")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val zoneId: Long,
    val zoneName: String,
    val enterTime: Long,        // epoch millis
    var exitTime: Long? = null, // null dopóki nie wyszedł ze strefy
    var durationMillis: Long? = null
)
