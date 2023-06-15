package com.eazy.daiku.ui.customer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eazy.daiku.R
import com.eazy.daiku.ui.customer.model.SearchMapHistoryModel

class HistorySearMapAdapter :
    RecyclerView.Adapter<HistorySearMapAdapter.HistorySearchMapHolder>() {
    private var listHistorySearchMap = ArrayList<SearchMapHistoryModel>()
    lateinit var onSelectRowHistorySearchMap: (SearchMapHistoryModel, Boolean) -> Unit

    fun add(listMap: ArrayList<SearchMapHistoryModel>) {
        clear()
        this.listHistorySearchMap = listMap
    }

    fun clear() {
        this.listHistorySearchMap.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorySearchMapHolder {
        return HistorySearchMapHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.history_search_map_adapter_layout, parent, false))
    }

    override fun onBindViewHolder(holder: HistorySearchMapHolder, position: Int) {
        val mode = listHistorySearchMap[position]
        holder.bind(mode)
        holder.actionDelete.setOnClickListener {
            onSelectRowHistorySearchMap.invoke(mode, true)
        }

        holder.itemView.setOnClickListener {
            onSelectRowHistorySearchMap.invoke(mode, false)
        }

    }

    override fun getItemCount(): Int {
        return listHistorySearchMap.size
    }

    class HistorySearchMapHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleHistory: TextView = itemView.findViewById(R.id.history_title)
        var actionDelete: ImageView = itemView.findViewById(R.id.action_delete)
        fun bind(searchMapHistoryModel: SearchMapHistoryModel) {
            titleHistory.text = searchMapHistoryModel.searchTitle
        }

    }
}