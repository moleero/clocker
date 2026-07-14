package com.example.geofencetracker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: LogAdapter
    private var currentEntries: List<LogEntry> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        db = AppDatabase.getInstance(this)

        val recycler = findViewById<RecyclerView>(R.id.recyclerLog)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = LogAdapter(emptyList())
        recycler.adapter = adapter

        lifecycleScope.launch {
            db.logDao().getAll().collect { entries ->
                currentEntries = entries
                adapter.update(entries)
            }
        }

        findViewById<android.widget.Button>(R.id.btnExportCsv).setOnClickListener {
            exportCsv()
        }
    }

    private fun exportCsv() {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder("Strefa,Wejście,Wyjście,Czas (minuty)\n")
        currentEntries.forEach { e ->
            val start = sdf.format(Date(e.enterTime))
            val end = e.exitTime?.let { sdf.format(Date(it)) } ?: ""
            val minutes = (e.durationMillis ?: 0) / 60000
            sb.append("\"${e.zoneName}\",\"$start\",\"$end\",$minutes\n")
        }

        val file = File(getExternalFilesDir(null), "geofence_log.csv")
        file.writeText(sb.toString())

        Toast.makeText(this, "Zapisano: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }
}
