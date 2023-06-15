package com.eazy.daiku.utility.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.eazy.daiku.R
import com.eazy.daiku.data.model.HomeScreenModel
import com.eazy.daiku.data.model.server_model.VehicleTypeRespond
import com.eazy.daiku.utility.ImageHelper
import com.eazy.daiku.utility.enumerable.HomeScreenActionEnum
import com.google.android.material.card.MaterialCardView
import java.util.ArrayList

class HomeScreenAdapter : RecyclerView.Adapter<HomeScreenAdapter.HomeScreenHolder>() {
    private var homeScreenModel = ArrayList<HomeScreenModel>()
    var selectedRow: ((HomeScreenModel) -> Unit?)? = null

    fun addHomeScreen(homeScreenModel: ArrayList<HomeScreenModel>) {
        this.homeScreenModel.clear()
        this.homeScreenModel = homeScreenModel
        notifyDataSetChanged()
    }

    fun addHomeScreen(homeScreenModel: HomeScreenModel) {
        this.homeScreenModel.add(homeScreenModel)
        this.homeScreenModel.sortBy {
            it.id
        }
        notifyDataSetChanged()
    }


    fun updateView(homeActionEnum: HomeScreenActionEnum, isEnableUi: Boolean) {
        for (home in homeScreenModel) {
            if (home.actionEnum.toString() == homeActionEnum.toString()) {
                val index = homeScreenModel.indexOf(home)
                home.isEnable = isEnableUi
                homeScreenModel[index] = home
                break
            }
        }
        notifyDataSetChanged()
    }

    fun removeView(homeActionEnum: HomeScreenActionEnum) {
        var temp: HomeScreenModel? = null
        for (home in homeScreenModel) {
            if (home.actionEnum.toString() == homeActionEnum.toString()) {
                temp = home
                break
            }
        }
        //remove
        temp?.let {
            homeScreenModel.remove(temp)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeScreenHolder {
        return HomeScreenHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.home_screen_row_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: HomeScreenHolder, position: Int) {
        val model = homeScreenModel[position]
        holder.bind(model)
        holder.homeScreenContainer.setOnClickListener {
            selectedRow?.invoke(model)
        }
    }

    override fun getItemCount(): Int {
        return homeScreenModel.size
    }

    class HomeScreenHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val iconImg: ImageView = itemView.findViewById(R.id.icon)
        private val titleTv: TextView = itemView.findViewById(R.id.title_tv)
        val homeScreenContainer: CardView =
            itemView.findViewById(R.id.home_screen_container)

        fun bind(homeScreenModel: HomeScreenModel) {
            titleTv.text = homeScreenModel.name
            ImageHelper.loadImage(
                itemView.context,
                homeScreenModel.icon,
                iconImg
            )

            homeScreenContainer.isEnabled = homeScreenModel.isEnable
            homeScreenContainer.alpha = if (homeScreenModel.isEnable) 1f else 0.2f

        }

    }

}