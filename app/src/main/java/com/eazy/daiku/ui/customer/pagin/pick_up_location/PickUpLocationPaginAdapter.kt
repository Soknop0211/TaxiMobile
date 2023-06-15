package com.eazy.daiku.ui.customer.pagin.pick_up_location

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.eazy.daiku.R
import com.eazy.daiku.data.model.server_model.History
import com.eazy.daiku.data.model.server_model.VehicleTypeRespond
import com.eazy.daiku.ui.customer.model.PickUpLocationModel
import com.eazy.daiku.ui.customer.step_booking.SelectCarBookingFragment
import com.eazy.daiku.utility.ImageHelper

class PickUpLocationPaginAdapter :
    RecyclerView.Adapter<PickUpLocationPaginAdapter.MyPickUpLocationPaginViewHolder>()
/* PagedListAdapter<
         PickUpLocationModel,
         PickUpLocationPaginAdapter.MyPickUpLocationPaginViewHolder
         >(USER_COMPARATOR)*/ {
    private var listPickUpLocation = ArrayList<PickUpLocationModel>()
    lateinit var selectRowPickUpLocation: (PickUpLocationModel) -> Unit

    companion object {
        private val USER_COMPARATOR = object : DiffUtil.ItemCallback<PickUpLocationModel>() {
            override fun areItemsTheSame(
                oldItem: PickUpLocationModel,
                newItem: PickUpLocationModel,
            ): Boolean {
                return false
            }

            override fun areContentsTheSame(
                oldItem: PickUpLocationModel,
                newItem: PickUpLocationModel,
            ): Boolean =
                false
        }
    }

    fun add(
        pickUpLocation: ArrayList<PickUpLocationModel>,
    ) {
        clear()
        this.listPickUpLocation = pickUpLocation
    }

    fun clear() {
        this.listPickUpLocation.clear()
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MyPickUpLocationPaginViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pick_up_location_adapter_layout, parent, false)
        val holder = MyPickUpLocationPaginViewHolder(
            view
        )
        return holder
    }

    override fun onBindViewHolder(holder: MyPickUpLocationPaginViewHolder, position: Int) {
        //val transaction: PickUpLocationModel? = getItem(position)
        val model = listPickUpLocation[position]
        holder.bind(model)
        holder.itemView.setOnClickListener {
            selectRowPickUpLocation.invoke(model)
        }

    }

    override fun onViewRecycled(holder: MyPickUpLocationPaginViewHolder) {
        super.onViewRecycled(holder)
    }

    class MyPickUpLocationPaginViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private var imageView: ImageView = itemView.findViewById(R.id.image_view)
        private var title: TextView = itemView.findViewById(R.id.tv_title)
        private var description: TextView = itemView.findViewById(R.id.description)
        fun bind(pickUp: PickUpLocationModel) {
            ImageHelper.loadImage(
                itemView.context,
                pickUp.imageView,
                imageView
            )
            title.text = pickUp.title
            description.text = pickUp.description

        }
    }

    override fun getItemCount(): Int {
        return listPickUpLocation.size
    }
}