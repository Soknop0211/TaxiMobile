package com.eazy.daiku.ui.customer.step_booking

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.core.view.marginTop
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.eazy.daiku.R
import com.eazy.daiku.databinding.TripDetailBookingLayoutBinding
import com.eazy.daiku.ui.customer.model.PreviewCheckoutModel
import com.eazy.daiku.ui.customer.viewmodel.ProcessCustomerBookingViewModel
import com.eazy.daiku.utility.AmountHelper
import com.eazy.daiku.utility.BetterActivityResult
import com.eazy.daiku.utility.base.BaseCoreActivity
import com.eazy.daiku.utility.base.BaseFragment
import com.eazy.daiku.utility.custom.MessageUtils
import com.eazy.daiku.utility.permission_media.PermissionHelper
import com.eazy.daiku.utility.redirect.RedirectClass
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TripDetailBookingFragment : BaseFragment() {
    private lateinit var binding: TripDetailBookingLayoutBinding
    private lateinit var fContext: FragmentActivity
    private var previewCheckoutModel: PreviewCheckoutModel? = null
    private var carId: String? = null
    private var deviceId: String? = null
    private var pathMapScreenShot: String? = null
    private var adminArea: String? = null
    private var countryName: String? = null
    private var getAddressLine: String? = null
    private var lattitude: String? = null
    private var longitude: String? = null
    private var vechicle: String? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
    }

    private val viewModel: ProcessCustomerBookingViewModel by viewModels {
        factory
    }


    companion object {
        @JvmStatic
        fun newInstance(
            previewCheckoutModel: PreviewCheckoutModel,
            carId: String,
            deviceId: String,
            pathMapScreenShot: String,
            adminArea: String,
            countryName: String,
            getAddressLine: String,
            lattitude: String,
            longtitude: String,
            vechicle: String,

            ) =
            TripDetailBookingFragment().apply {
                this.previewCheckoutModel = previewCheckoutModel
                this.carId = carId
                this.deviceId = deviceId
                this.pathMapScreenShot = pathMapScreenShot
                this.adminArea = adminArea
                this.countryName = countryName
                this.getAddressLine = getAddressLine
                this.lattitude = lattitude
                this.longitude = longtitude
                this.vechicle = vechicle
            }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = TripDetailBookingLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initObser()
        initAction()
    }

    private fun initView(view: View) {
        binding.tvDestination.text = String.format("%s", adminArea)
        binding.tvDistance.text = String.format("%sKm",
            previewCheckoutModel?.kilometre?.let { AmountHelper.amountFormat("", it) })
        binding.tvTotal.text = String.format("%s",
            previewCheckoutModel?.totalPrice?.let { AmountHelper.amountFormat("$", it) })
        Glide.with(this).load(pathMapScreenShot)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true).error(R.drawable.empty_image)
            .into(binding.imLocationView)
        binding.tvAdminArea.text = String.format("%s,%s", adminArea, countryName)
        binding.tvGetLineAddress.text =
            String.format("%s: %s", getString(R.string.address), getAddressLine)
        binding.tvLatitude.text = String.format("Lattitude: %s",
            lattitude?.toDouble()?.let { AmountHelper.latLngFormat(it) })
        binding.tvLongitude.text = String.format("Longtitude: %s",
            longitude?.toDouble()?.let { AmountHelper.latLngFormat(it) })
        binding.tvVehicle.text = String.format("%s", vechicle)
        var date: LocalDateTime? = null
        var formatter: DateTimeFormatter? = null
        var formatted: String? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            date = LocalDateTime.now()
            formatter = DateTimeFormatter.ofPattern("MMM-dd-yyyy")
            formatted = date.format(formatter)
        }
        binding.tvDate.text = String.format("%s", formatted)
        val layoutView: LinearLayout = view.findViewById(R.id.container_vat)
        previewCheckoutModel?.serviceFees.let { listServiceFees ->
            listServiceFees?.size.let {
                if (it != null && it > 0 && listServiceFees != null) {
                    for (previewCheckoutModel in listServiceFees) {
                        val viewGroup: View = layoutInflater.inflate(R.layout.layout_vat, null)
                        val tvTitle: TextView = viewGroup.findViewById(R.id.tv_title_vat)
                        val tvVat: TextView = viewGroup.findViewById(R.id.tv_vat)
                        var vat: String
                        if (previewCheckoutModel.label.equals("VAT")) {
                            vat = getString(R.string.vat)
                        } else {
                            vat = previewCheckoutModel.label.toString()
                        }

                        tvTitle.text = String.format("%s", vat)
                        tvVat.text = String.format("%s", previewCheckoutModel.displayRate)
                        layoutView.addView(viewGroup)
                    }
                }
            }
        }
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun initObser() {
        viewModel.loadingCheckoutLiveData.observe(fContext) {
            binding.loadingView.root.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.dataCheckoutLiveData.observe(fContext) {
            if (it != null && it.success) {
                val dataJson = Gson().toJson(it.data)
                RedirectClass.gotoWebPayActivity(fContext, dataJson,object :BetterActivityResult.OnActivityResult<ActivityResult>{
                    override fun onActivityResult(result: ActivityResult) {
                        TODO("Not yet implemented")
                    }

                })
            } else {
                MessageUtils.showError(fContext, "", it.message)
            }
        }
    }


    private fun initAction() {
        binding.actionBack.setOnClickListener {

        }
        binding.actionCancel.setOnClickListener {
            fContext.finish()
        }

        binding.actionPurchase.setOnClickListener {
            if (PermissionHelper.hasDeviceGpsAndNetwork(fContext)) {
                if (PermissionHelper.hasCOARSEAndFINELocationPermission(fContext)) {
                    submitCheckout()
                } else {
                    PermissionHelper.requestMultiPermission(
                        fContext,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    ) { hasPermission ->
                        if (hasPermission) {
                            submitCheckout()
                        } else {
                            MessageUtils.showError(
                                fContext,
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

    private fun submitCheckout() {
        val latitude = previewCheckoutModel?.toLocation?.get(0)
        val longitude = previewCheckoutModel?.toLocation?.get(1)
        val body = HashMap<String, Any>()
        val bodyMap = HashMap<String, Any>()
        body["car_id"] = carId.toString()
        body["select_map"] = 1
        body["device_id"] = deviceId.toString()
        bodyMap["title"] = previewCheckoutModel?.title.toString()
        bodyMap["latitude"] = latitude.toString()
        bodyMap["longitude"] = longitude.toString()
        body["map"] = bodyMap
        viewModel.submitCheckout(body)
    }

}