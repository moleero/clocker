package com.example.geofencetracker

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ZoneDao {
    @Query("SELECT * FROM zones ORDER BY name")
    fun getAll(): Flow<List<Zone>>

    @Query("SELECT * FROM zones WHERE id = :id")
    suspend fun getById(id: Long): Zone?

    @Insert
    suspend fun insert(zone: Zone): Long

    @Delete
    suspend fun delete(zone: Zone)
}

@Dao
interface LogDao {
    @Query("SELECT * FROM log_entries ORDER BY enterTime DESC")
    fun getAll(): Flow<List<LogEntry>>

    @Query("SELECT * FROM log_entries WHERE zoneId = :zoneId AND exitTime IS NULL LIMIT 1")
    suspend fun getOpenEntry(zoneId: Long): LogEntry?

    @Insert
    suspend fun insert(entry: LogEntry): Long

    @Update
    suspend fun update(entry: LogEntry)
}
