package com.example.geofencetracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zones")
data class Zone(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float
)
