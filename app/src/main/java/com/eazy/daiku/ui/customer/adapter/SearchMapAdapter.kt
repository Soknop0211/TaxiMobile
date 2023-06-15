package com.eazy.daiku.ui.customer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eazy.daiku.R
import com.eazy.daiku.data.model.server_model.VehicleTypeRespond
import com.eazy.daiku.ui.customer.model.Features
import com.eazy.daiku.ui.customer.model.Geometry
import com.eazy.daiku.ui.customer.model.SearchMapRespondModel
import com.eazy.daiku.ui.customer.model.sear_map.Results

class SearchMapAdapter : RecyclerView.Adapter<SearchMapAdapter.SelectCarBookingHolder>() {
    private var listMap = ArrayList<Results>()
    lateinit var onSelectRowSearchMap: (Results) -> Unit

    fun add(listMap: ArrayList<Results>) {
        clear()
        this.listMap = listMap
    }

    fun clear() {
        this.listMap.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectCarBookingHolder {
        return SelectCarBookingHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.search_map_adapter_layout, parent, false))
    }

    override fun onBindViewHolder(holder: SelectCarBookingHolder, position: Int) {
        val mode = listMap[position]
        holder.bind(mode)
        holder.itemView.setOnClickListener {
            onSelectRowSearchMap.invoke(mode)
        }

    }

    override fun getItemCount(): Int {
        return listMap.size
    }

    class SelectCarBookingHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.history_title)
        private val description: TextView = itemView.findViewById(R.id.history_description)
        fun bind(results: Results) {
            title.text = results.name
            description.text = results.formattedAddress
        }

    }
}