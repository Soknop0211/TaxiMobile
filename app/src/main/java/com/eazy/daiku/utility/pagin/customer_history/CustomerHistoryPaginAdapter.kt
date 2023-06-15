package com.eazy.daiku.utility.pagin.customer_history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.R
import com.eazy.daiku.data.model.server_model.CustomerHistoryModel
import com.eazy.daiku.data.model.server_model.History
import com.eazy.daiku.data.model.server_model.User
import com.eazy.daiku.utility.*
import com.eazy.daiku.utility.other.AppLOGG
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson


class CustomerHistoryPaginAdapter(
) :
    PagedListAdapter<
            CustomerHistoryModel,
            CustomerHistoryPaginAdapter.MyHistoryPaginViewHolder
            >(USER_COMPARATOR) {

    lateinit var selectRowTrx: (CustomerHistoryModel) -> Unit
    lateinit var selectRowQr: (CustomerHistoryModel) -> Unit


    companion object {
        private val USER_COMPARATOR = object : DiffUtil.ItemCallback<CustomerHistoryModel>() {
            override fun areItemsTheSame(
                oldItem: CustomerHistoryModel,
                newItem: CustomerHistoryModel,
            ): Boolean {
                return false
            }

            override fun areContentsTheSame(
                oldItem: CustomerHistoryModel,
                newItem: CustomerHistoryModel,
            ): Boolean =
                false
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MyHistoryPaginViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_row_layout, parent, false)
        val holder = MyHistoryPaginViewHolder(
            view
        )
        return holder
    }

    override fun onBindViewHolder(holder: MyHistoryPaginViewHolder, position: Int) {
        val transaction: CustomerHistoryModel? = getItem(position)
        transaction?.let { history ->
            holder.bind(history)
            holder.itemView.setOnClickListener {
                selectRowTrx.invoke(history)
            }
            holder.map?.setOnClickListener {
                holder.itemView.performClick()
            }
            holder.actionShowQr.setOnClickListener {
                selectRowQr.invoke(history)
            }


        }
    }

    override fun onViewRecycled(holder: MyHistoryPaginViewHolder) {
        super.onViewRecycled(holder)
        holder.gMap?.clear()
        holder.gMap?.mapType = GoogleMap.MAP_TYPE_NONE
    }

    class MyHistoryPaginViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), OnMapReadyCallback {
        private val dateHistoryTv: TextView = itemView.findViewById(R.id.date_history_tv)
        private val timeHistoryTv: TextView = itemView.findViewById(R.id.time_history_tv)
        private val hotelNameHistoryTv: TextView = itemView.findViewById(R.id.hotel_name_history_tv)
        private val amountTv: TextView = itemView.findViewById(R.id.amount_history_tv)
        private val distanceHistoryTv: TextView =
            itemView.findViewById(R.id.distance_hotel_history_tv)
        private val statusTv: TextView = itemView.findViewById(R.id.status_tv)
        private var historyGlobal: CustomerHistoryModel? = null
        val actionShowQr: LinearLayout = itemView.findViewById(R.id.action_show_qr)
        private var imQr: ImageView = itemView.findViewById(R.id.im_qr)

        val map: MapView? = itemView.findViewById(R.id.map_view)
        var gMap: GoogleMap? = null

        init {
            map?.let {
                map.onCreate(null)
                map.onResume()
                map.getMapAsync(this)
            }
        }

        fun bind(history: CustomerHistoryModel) {
            historyGlobal = history
            history.meta
            val dateHistory = history.createdAt ?: ""
            val dateHistoryStr = DateTimeHelper.dateFm(
                dateHistory,
                "yyyy-MM-dd HH:mm:ss",
                "EEEE, dd MMMM yyyy"
            )
            val timeHistory = history.createdAt ?: ""
            val timeHistoryStr = DateTimeHelper.dateFm(
                timeHistory,
                "yyyy-MM-dd HH:mm:ss",
                "hh:mm a"
            )
            actionShowQr.visibility =
                if (history.status != "Cancelled" && history.status != "Cus-Cancelled") View.VISIBLE else View.GONE
            dateHistoryTv.text = dateHistoryStr
            timeHistoryTv.text = timeHistoryStr
            hotelNameHistoryTv.text =
                if (history.meta?.destination?.title != null) String.format("%s",
                    history.meta?.destination?.title)
                else if (history.meta?.destination?.toTitle != null) String.format("%s",
                    history.meta?.destination?.toTitle)
                else if (history.meta?.destination?.fromTitle != null) String.format("%s",
                    history.meta?.destination?.fromTitle)
                else "---"
            /*  hotelNameHistoryTv.text = history.meta?.destination?.title ?: "---"*/
            distanceHistoryTv.text = String.format(
                "%sKm", history.meta?.destination?.distance?.let {
                    AmountHelper.amountFormat("",
                        it)
                }
                    ?: "---"
            )
            amountTv.text = AmountHelper.amountFormat(
                "$", history.total ?: 0.00
            )
            statusTv.text = history.status ?: "---"
            statusTv.setBackgroundColor(
                StatusColor.colorStatus(
                    itemView.context,
                    history.status ?: ""
                )
            )

            //to disable amount as wego employee
            if (BuildConfig.IS_WEGO_TAXI) {
                val jsonStr =
                    EazyTaxiHelper.getSharePreference(itemView.context, Constants.userDetailKey)
                if (jsonStr.isNotEmpty()) {
                    val user: User? = GsonConverterHelper.getJsonObjectToGenericClass(jsonStr)
                    val isWeGoEmployee = user?.isEmployee == Config.WeGoBusiness.Employee
                    amountTv.visibility = if (isWeGoEmployee) View.GONE else View.VISIBLE
                }
            } else {
                amountTv.visibility = View.VISIBLE
            }
        }

        override fun onMapReady(p0: GoogleMap?) {
            gMap = p0
            val hotel = LatLng(
                historyGlobal?.meta?.destination?.mapLat ?: 0.0,
                historyGlobal?.meta?.destination?.mapLng ?: 0.0
            )
            gMap?.let {
                it.moveCamera(CameraUpdateFactory.newLatLngZoom(hotel, 16.0f))
            }
            gMap?.let { googleMap ->
                googleMap.addMarker(
                    MarkerOptions()
                        .position(
                            hotel
                        )
                    /*.title(historyGlobal?.meta?.destination?.title ?: "---")*/
                ).showInfoWindow()
            }

        }
    }
}