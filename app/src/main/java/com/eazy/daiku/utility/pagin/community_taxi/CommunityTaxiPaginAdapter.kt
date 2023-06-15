package com.eazy.daiku.utility.pagin.community_taxi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.eazy.daiku.R
import com.eazy.daiku.data.model.server_model.CommunityTaxiRespond
import com.google.android.material.card.MaterialCardView

class CommunityTaxiPaginAdapter :
    PagedListAdapter<
            CommunityTaxiRespond,
            CommunityTaxiPaginAdapter.MyCommunityTaxiPaginViewHolder
            >(USER_COMPARATOR) {

    private var selectCommunityTaxiId = -1

    companion object {
        private val USER_COMPARATOR = object : DiffUtil.ItemCallback<CommunityTaxiRespond>() {
            override fun areItemsTheSame(
                oldItem: CommunityTaxiRespond,
                newItem: CommunityTaxiRespond
            ): Boolean {
                return false
            }

            override fun areContentsTheSame(
                oldItem: CommunityTaxiRespond,
                newItem: CommunityTaxiRespond
            ): Boolean =
                false
        }
    }

    fun submitData(pagedList: PagedList<CommunityTaxiRespond>, communityTaxiId: Int) {
        this.selectCommunityTaxiId = communityTaxiId
        submitList(pagedList)

    }

    fun updateItem(communityTaxiId: Int) {
        this.selectCommunityTaxiId = communityTaxiId
        notifyDataSetChanged()
    }

    fun getCommunityTaxiLists(): PagedList<CommunityTaxiRespond>? {
        return currentList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyCommunityTaxiPaginViewHolder {
        return MyCommunityTaxiPaginViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.community_taxi_row_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyCommunityTaxiPaginViewHolder, position: Int) {
        val communityTaxi: CommunityTaxiRespond? = getItem(position)
        communityTaxi?.let {
            holder.bind(communityTaxi)
            holder.selectItem(selectCommunityTaxiId == communityTaxi.id)
            holder.itemView.setOnClickListener {
                selectCommunityTaxiId = communityTaxi.id ?: -1
                notifyDataSetChanged()
                selectRow.invoke(communityTaxi)
            }
        }
    }

    lateinit var selectRow: (CommunityTaxiRespond) -> Unit

    class MyCommunityTaxiPaginViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTv: TextView = itemView.findViewById(R.id.title_tv)
        private val communityCardRow: MaterialCardView =
            itemView.findViewById(R.id.community_card_row)

        fun bind(communityTaxi: CommunityTaxiRespond) {
            titleTv.text = communityTaxi.businessName ?: "N/A"
        }

        fun selectItem(selectItem: Boolean) {
            communityCardRow.strokeColor = ContextCompat.getColor(
                itemView.context,
                if (selectItem) R.color.colorPrimary else R.color.white
            )
            communityCardRow.strokeWidth = if (selectItem) 3 else 0.95.toInt()
            communityCardRow.alpha = if (selectItem) 1f else 0.6f
        }
    }

}