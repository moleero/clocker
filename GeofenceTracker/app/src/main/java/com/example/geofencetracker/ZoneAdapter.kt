package com.example.geofencetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ZoneAdapter(
    private var zones: List<Zone>,
    private val onDelete: (Zone) -> Unit
) : RecyclerView.Adapter<ZoneAdapter.VH>() {

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
        val zone = zones[position]
        holder.title.text = zone.name
        holder.subtitle.text = "%.5f, %.5f — promień %.0fm".format(
            zone.latitude, zone.longitude, zone.radiusMeters
        )
        holder.itemView.setOnLongClickListener {
            onDelete(zone)
            true
        }
    }

    override fun getItemCount() = zones.size

    fun update(newZones: List<Zone>) {
        zones = newZones
        notifyDataSetChanged()
    }
}
