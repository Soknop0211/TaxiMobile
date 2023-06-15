package com.eazy.daiku.ui.customer.step_booking

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eazy.daiku.R
import com.eazy.daiku.data.model.server_model.Vehicle
import com.eazy.daiku.databinding.SelectCarBookingLayoutBinding
import com.eazy.daiku.ui.customer.model.ListKioskModel
import com.eazy.daiku.ui.customer.model.Terms
import com.eazy.daiku.ui.customer.viewmodel.ProcessCustomerBookingViewModel
import com.eazy.daiku.utility.AmountHelper
import com.eazy.daiku.utility.ImageHelper
import com.eazy.daiku.utility.base.BaseCoreActivity
import com.eazy.daiku.utility.base.BaseFragment
import com.eazy.daiku.utility.custom.MessageUtils
import com.eazy.daiku.utility.permission_media.PermissionHelper
import com.eazy.daiku.utility.redirect.RedirectClass
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson

class SelectCarBookingFragment : BaseFragment() {
    private lateinit var binding: SelectCarBookingLayoutBinding
    lateinit var fContext: FragmentActivity
    private lateinit var navController: NavController
    private lateinit var selectCarBookingAdapter: SelectCarBookingAdapter
    private var listKioskModel: ListKioskModel? = null
    private var latitude: String? = null
    private var longitude: String? = null
    private var address: String? = null
    private var carId: String? = null
    private var pathScreenShotMap: String? = null
    private var adminArea: String? = null
    private var countryName: String? = null
    private var getAddressLine: String? = null
    private var vechicle: String? = null
    private val viewModel: ProcessCustomerBookingViewModel by viewModels {
        factory
    }

    companion object {
        @JvmStatic
        fun newInstance(
            listKioskModel: ListKioskModel,
            latitude: String,
            longitude: String,
            address: String,
            pathScreenShotMap: String,
            adminArea: String,
            countryName: String,
            getAddressLine: String,
        ) =
            SelectCarBookingFragment().apply {
                this.listKioskModel = listKioskModel
                this.latitude = latitude
                this.longitude = longitude
                this.address = address
                this.pathScreenShotMap = pathScreenShotMap
                this.adminArea = adminArea
                this.countryName = countryName
                this.getAddressLine = getAddressLine
            }

        const val myListTaxiKey = "myListTaxiKey"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = SelectCarBookingLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        initAction()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        // listKioskModel?.let { selectCarBookingAdapter.add(it.terms) }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = selectCarBookingAdapter
        }
    }

    private fun initView() {
        selectCarBookingAdapter = SelectCarBookingAdapter()
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun initObserver() {

        viewModel.loadingCheckoutTaxiLiveData.observe(self()) {
            binding.loadingView.root.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.dataCheckoutTaxiLiveData.observe(self()) {
            if (it != null && it.success) {
                val dataJson = Gson()
                val previewCheckout = dataJson.toJson(it.data)
                RedirectClass.gotoPreviewCheckoutActivity(fContext,
                    previewCheckout,
                    carId.toString(),
                    listKioskModel?.deviceId.toString(),
                    pathScreenShotMap.toString(),
                    adminArea.toString(),
                    countryName.toString(),
                    getAddressLine.toString(),
                    latitude.toString(),
                    longitude.toString(),
                    vechicle.toString())
            } else {
                MessageUtils.showError(fContext, "", it.message)
            }
        }

    }

    private fun initAction() {
        binding.actionClose.setOnClickListener {
            accessParentFragment()?.dismiss()
        }

        selectCarBookingAdapter.onSelectRow = { carBookingModel ->
            carId = carBookingModel.id.toString()
            vechicle = carBookingModel.name
            if (PermissionHelper.hasDeviceGpsAndNetwork(fContext)) {
                if (PermissionHelper.hasCOARSEAndFINELocationPermission(fContext)) {
                    //submitCheckoutTaxi(carBookingModel)
                } else {
                    PermissionHelper.requestMultiPermission(
                        self(),
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    ) { hasPermission ->
                        if (hasPermission) {
                            // submitCheckoutTaxi(carBookingModel)
                        } else {
                            MessageUtils.showError(
                                self(),
                                null,
                                "Permission Deny!"
                            )
                        }
                    }
                }
            } else {
                if (fContext is BaseCoreActivity) {
                    (fContext as BaseCoreActivity).globalRequestDeviceGps()
                }
            }
        }

    }

    private fun submitCheckoutTaxi(terms: Terms) {
        val body = HashMap<String, Any>()
        val bodyMap = HashMap<String, Any>()
        body["car_id"] = terms.id.toString()
        body["select_map"] = 1
        body["device_id"] = listKioskModel?.deviceId.toString()
        bodyMap["title"] = address.toString()
        bodyMap["latitude"] = latitude.toString()
        bodyMap["longitude"] = longitude.toString()
        body["map"] = bodyMap
        viewModel.submitCheckoutTaxi(body)
    }


    private fun accessParentFragment(): StepBookingFragmentBottomSheet? {
        if (fContext.supportFragmentManager.findFragmentByTag("StepBookingFragmentBottomSheet") is StepBookingFragmentBottomSheet) {
            return fContext.supportFragmentManager.findFragmentByTag("StepBookingFragmentBottomSheet") as StepBookingFragmentBottomSheet

        }
        return null
    }


    class SelectCarBookingAdapter :
        RecyclerView.Adapter<SelectCarBookingAdapter.SelectCarBookingHolder>() {
        private var carBookingList: ArrayList<Vehicle>? = null
        lateinit var onSelectRow: (Vehicle) -> Unit
        private lateinit var context: Context
        private val selected = ArrayList<Int>()


        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            this.context = recyclerView.context
        }

        fun add(
            carBookingList: ArrayList<Vehicle>,
        ) {
            clear()
            this.carBookingList = carBookingList
        }

        fun clear() {
            this.carBookingList?.clear()
        }

        private fun notifyItemByPosition(selectedPosition: Int) {
            if (selected.isEmpty()) {
                selected.add(selectedPosition)
            }
            selected.clear()
            selected.add(selectedPosition)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectCarBookingHolder {

            return SelectCarBookingHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.select_car_booing_adapter_layout, parent, false))
        }

        override fun getItemCount(): Int {
            return carBookingList?.size ?: 0
        }


        override fun onBindViewHolder(holder: SelectCarBookingHolder, position: Int) {
            val model = carBookingList?.get(position)
            val selectedColor =
                ContextCompat.getDrawable(context, R.drawable.selectable_item_background)
            val unsSelectedColor = ContextCompat.getDrawable(context,
                R.drawable.unselectable_item_background)

            if (!selected.contains(position)) {
                holder.containerVehicle.background = unsSelectedColor
            } else {
                holder.containerVehicle.background = selectedColor
            }

            if (model != null) {
                holder.bind(model)
            }

            holder.itemView.setOnClickListener {
                if (model != null) {
                    notifyItemByPosition(position)
                    onSelectRow.invoke(model)
                }
            }

        }

        class SelectCarBookingHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val nameCar: TextView = itemView.findViewById(R.id.tv_name_car)
            private val imCar: ImageView = itemView.findViewById(R.id.im_car)
            private val tvPrice: TextView = itemView.findViewById(R.id.tv_price)
            val containerVehicle: LinearLayout = itemView.findViewById(R.id.container_vehicle)
            fun bind(vehicle: Vehicle) {
                nameCar.text = vehicle.name ?: "n/a"
                tvPrice.text = String.format("%s",
                    vehicle.price?.let { AmountHelper.amountFormat("USD", it) })
                ImageHelper.loadImage(
                    itemView.context,
                    vehicle.image ?: R.drawable.sedan,
                    imCar
                )

            }
        }

    }

}

