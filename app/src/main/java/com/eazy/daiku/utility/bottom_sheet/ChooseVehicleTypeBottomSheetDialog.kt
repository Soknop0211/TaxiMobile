package com.eazy.daiku.utility.bottom_sheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eazy.daiku.R
import com.eazy.daiku.data.model.server_model.VehicleTypeRespond
import com.eazy.daiku.databinding.ChooseVehicleTypeTaxiRowBottomSheetLayoutBinding
import com.eazy.daiku.utility.ImageHelper
import com.eazy.daiku.utility.base.BaseBottomSheetDialogFragment
import com.eazy.daiku.utility.other.AppLOGG
import com.google.android.material.card.MaterialCardView

class ChooseVehicleTypeBottomSheetDialog : BaseBottomSheetDialogFragment() {
    private lateinit var binding: ChooseVehicleTypeTaxiRowBottomSheetLayoutBinding
    private lateinit var fContext: FragmentActivity
    private lateinit var selectVehicleTaxiListener: (VehicleTypeRespond) -> Unit
    private lateinit var vehicleAdapter: VehicleChooseAdapter

    private var vehicleTypeTaxis: ArrayList<VehicleTypeRespond>? = null

    private var vehicleTermId: Int = -1

    companion object {
        @JvmStatic
        fun newInstance(
            vehicleTermId: Int,
            vehicleTypeTaxis: ArrayList<VehicleTypeRespond>,
            selectVehicleTaxiListener: (VehicleTypeRespond) -> Unit
        ) =
            ChooseVehicleTypeBottomSheetDialog().apply {
                this.vehicleTermId = vehicleTermId
                this.vehicleTypeTaxis = vehicleTypeTaxis
                this.selectVehicleTaxiListener = selectVehicleTaxiListener
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChooseVehicleTypeTaxiRowBottomSheetLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        doAction()
        initData()
    }


    private fun initView() {
        vehicleAdapter = VehicleChooseAdapter()
        binding.vehicleTypeTaxiRecyclerView.apply {
            layoutManager = GridLayoutManager(
                fContext,
                2
            )
            adapter = vehicleAdapter
        }

    }

    private fun doAction() {
        vehicleAdapter.selectedRow = {
            this.vehicleTermId = it.id ?: -1
            selectVehicleTaxiListener.invoke(it)
        }
        binding.actionCloseImg.setOnClickListener {
            dialog?.dismiss()
        }
    }

    private fun initData() {
        vehicleTypeTaxis?.let {
            vehicleAdapter.addVehicle(it, vehicleTermId)
        }
    }

    private class VehicleChooseAdapter :
        RecyclerView.Adapter<VehicleChooseAdapter.VehicleHolder>() {
        private var vehicleModels: ArrayList<VehicleTypeRespond>? = null
        lateinit var selectedRow: (VehicleTypeRespond) -> Unit
        private var selectedVehicleTypeId = -1

        fun addVehicle(
            vehicleModels: ArrayList<VehicleTypeRespond>,
            vehicleTypeId: Int
        ) {
            this.vehicleModels = vehicleModels
            this.selectedVehicleTypeId = vehicleTypeId

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
                AppLOGG.d("CaiFuTonLOGG", "$selectedVehicleTypeId ## ${model.id}")

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
}