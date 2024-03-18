package com.example.assignmentkotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LocationAdapter(private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {
    val locations: MutableList<LocationData> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]
        holder.bind(location)
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return locations.size
    }

    fun addLocation(location: LocationData) {
        locations.add(location)
        notifyItemInserted(locations.size - 1)
    }

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val latitudeTextView: TextView = itemView.findViewById(R.id.latitudeTextView)
        private val longitudeTextView: TextView = itemView.findViewById(R.id.longitudeTextView)

        fun bind(location: LocationData) {
            latitudeTextView.text = "Latitude: ${location.latitude}"
            longitudeTextView.text = "Longitude: ${location.longitude}"
        }
    }
}

