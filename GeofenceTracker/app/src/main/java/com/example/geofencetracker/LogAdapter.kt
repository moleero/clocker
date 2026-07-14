package com.example.geofencetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogAdapter(private var entries: List<LogEntry>) : RecyclerView.Adapter<LogAdapter.VH>() {

    private val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(android.R.id.text1)
        val subtitle: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val entry = entries[position]
        holder.title.text = entry.zoneName
        val start = sdf.format(Date(entry.enterTime))
        val sub = if (entry.exitTime != null) {
            val end = sdf.format(Date(entry.exitTime!!))
            val mins = (entry.durationMillis ?: 0) / 60000
            "$start → $end  (${mins / 60}h ${mins % 60}m)"
        } else {
            "$start → w trakcie"
        }
        holder.subtitle.text = sub
    }

    override fun getItemCount() = entries.size

    fun update(newEntries: List<LogEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}
