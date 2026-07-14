package com.example.geofencetracker

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var geofenceHelper: GeofenceHelper
    private lateinit var adapter: ZoneAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getInstance(this)
        geofenceHelper = GeofenceHelper(this)

        val recycler = findViewById<RecyclerView>(R.id.recyclerZones)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ZoneAdapter(emptyList()) { zone -> confirmDelete(zone) }
        recycler.adapter = adapter

        lifecycleScope.launch {
            db.zoneDao().getAll().collect { zones -> adapter.update(zones) }
        }

        findViewById<android.widget.Button>(R.id.btnAddZone).setOnClickListener {
            checkPermissionsThen { showAddZoneDialog() }
        }

        findViewById<android.widget.Button>(R.id.btnShowLog).setOnClickListener {
            startActivity(Intent(this, LogActivity::class.java))
        }

        checkPermissionsThen { startForegroundService(Intent(this, TrackingForegroundService::class.java)) }
    }

    private fun showAddZoneDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_zone, null)
        val editName = view.findViewById<EditText>(R.id.editName)
        val editLat = view.findViewById<EditText>(R.id.editLat)
        val editLng = view.findViewById<EditText>(R.id.editLng)
        val editRadius = view.findViewById<EditText>(R.id.editRadius)

        AlertDialog.Builder(this)
            .setTitle("Nowa strefa")
            .setView(view)
            .setPositiveButton("Dodaj") { _, _ ->
                val name = editName.text.toString().ifBlank { "Strefa" }
                val lat = editLat.text.toString().toDoubleOrNull()
                val lng = editLng.text.toString().toDoubleOrNull()
                val radius = editRadius.text.toString().toFloatOrNull() ?: 100f

                if (lat == null || lng == null) {
                    Toast.makeText(this, "Podaj poprawne współrzędne", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val zone = Zone(name = name, latitude = lat, longitude = lng, radiusMeters = radius)
                lifecycleScope.launch {
                    val id = db.zoneDao().insert(zone)
                    val saved = zone.copy(id = id)
                    geofenceHelper.registerZone(saved) { ok ->
                        if (!ok) runOnUiThread {
                            Toast.makeText(this@MainActivity, "Nie udało się zarejestrować strefy", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun confirmDelete(zone: Zone) {
        AlertDialog.Builder(this)
            .setTitle("Usunąć strefę \"${zone.name}\"?")
            .setPositiveButton("Usuń") { _, _ ->
                geofenceHelper.removeZone(zone.id)
                lifecycleScope.launch { db.zoneDao().delete(zone) }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun checkPermissionsThen(action: () -> Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
            return
        }

        // Background location trzeba prosić osobno na Androidzie 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            AlertDialog.Builder(this)
                .setTitle("Wymagane uprawnienie")
                .setMessage("Aby śledzić strefy w tle, wybierz \"Zezwalaj cały czas\" w ustawieniach lokalizacji.")
                .setPositiveButton("OK") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 101
                    )
                }
                .show()
            return
        }

        action()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            checkPermissionsThen { }
        }
    }
}
