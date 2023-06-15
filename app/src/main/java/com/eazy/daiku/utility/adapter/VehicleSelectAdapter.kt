package com.eazy.daiku.utility.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.eazy.daiku.R
import com.eazy.daiku.data.model.server_model.VehicleTypeRespond
import com.eazy.daiku.utility.ImageHelper
import com.google.android.material.card.MaterialCardView
import java.util.ArrayList

class VehicleSelectAdapter : RecyclerView.Adapter<VehicleSelectAdapter.VehicleHolder>() {
    private var vehicleModels: ArrayList<VehicleTypeRespond>? = null
    lateinit var selectedRow: (VehicleTypeRespond) -> Unit
    private var selectedVehicleTypeId = -1

    fun addVehicle(vehicleModels: ArrayList<VehicleTypeRespond>,selectedVehicleTypeId: Int) {
        this.vehicleModels = vehicleModels
        this.selectedVehicleTypeId = selectedVehicleTypeId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleHolder {
        return VehicleHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.vehicle_select_row_, parent, false)
        )
    }

    override fun onBindViewHolder(holder: VehicleHolder, position: Int) {
        val model = vehicleModels?.get(position)
        if (model != null) {
            holder.bind(model)
            holder.selectItem(selectedVehicleTypeId == model.id)
            holder.cardVehicleCardView.setOnClickListener {
                selectedVehicleTypeId = model.id
                notifyDataSetChanged()
                selectedRow.invoke(model)
            }
        }
    }

    override fun getItemCount(): Int {
        return vehicleModels?.size ?: 0
    }

    class VehicleHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById(R.id.vehicle_icon_img)
        val textView: TextView = itemView.findViewById(R.id.vehicle_name_tv)
        val cardVehicleCardView: MaterialCardView =
            itemView.findViewById(R.id.card_vehicle_card_view)

        fun bind(vehicleModel: VehicleTypeRespond) {

            textView.text = vehicleModel.name ?: "n/a"
            ImageHelper.loadImage(
                itemView.context,
                vehicleModel.image ?: "",
                imageView
            )
        }

        fun selectItem(selectItem: Boolean) {
            cardVehicleCardView.strokeColor = ContextCompat.getColor(
                itemView.context,
                if (selectItem) R.color.colorPrimary else R.color.white
            )
            cardVehicleCardView.strokeWidth = if (selectItem) 3 else 0.95.toInt()
            cardVehicleCardView.alpha = if (selectItem) 1f else 0.6f
        }
    }

}