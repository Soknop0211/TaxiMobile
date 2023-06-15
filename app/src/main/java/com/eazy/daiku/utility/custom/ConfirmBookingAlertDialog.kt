package com.eazy.daiku.utility.custom

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.eazy.daiku.R
import com.eazy.daiku.data.model.BookingTaxiModel
import com.eazy.daiku.data.model.server_model.DestinationInfo
import com.eazy.daiku.databinding.ConfirmBookingLayoutBinding
import com.eazy.daiku.utility.AmountHelper
import com.eazy.daiku.utility.enumerable.ConfirmKey
import com.eazy.daiku.utility.permission_media.PermissionHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class ConfirmBookingAlertDialog :
    DialogFragment(),
    OnMapReadyCallback {

    private lateinit var binding: ConfirmBookingLayoutBinding
    private lateinit var fContext: FragmentActivity
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var selectActionListener: (ConfirmKey, Dialog?, String, DestinationInfo?) -> Unit

    private var bookingTaxiModel: BookingTaxiModel? = null
    private var googleMap: GoogleMap? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(
            bookingTaxiModel: BookingTaxiModel,
            selectActionListener: (ConfirmKey, Dialog?, String, DestinationInfo?) -> Unit
        ) =
            ConfirmBookingAlertDialog().apply {
                this.bookingTaxiModel = bookingTaxiModel
                this.selectActionListener = selectActionListener
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = ConfirmBookingLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initData()
        doAction()
    }

    private fun initView() {
        dialog?.setCanceledOnTouchOutside(false)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_fragment_1) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initData() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(fContext)
        binding.titleTv.text = bookingTaxiModel?.destinationInfo?.title ?: bookingTaxiModel?.destinationInfo?.toTitle ?: "---"
        binding.codeTv.text = bookingTaxiModel?.bookingCode ?: "---"
        binding.usernameTv.text = bookingTaxiModel?.customerInfo?.name ?: "---"
        binding.phoneTv.text = bookingTaxiModel?.customerInfo?.phone ?: "---"
        binding.distanceTv.text =
            String.format(Locale.US, "%sKm", bookingTaxiModel?.destinationInfo?.distance ?: "---")
        binding.totalTv.text = AmountHelper.amountFormat(
            "$", bookingTaxiModel?.destinationInfo?.total ?: 0.00
        )

        binding.usernameTableRow.visibility =
            if (bookingTaxiModel?.customerInfo == null) View.GONE else View.VISIBLE
        binding.phoneTableRow.visibility =
            if (bookingTaxiModel?.customerInfo == null) View.GONE else View.VISIBLE

    }

    private fun doAction() {
        binding.actionRejectMtb.setOnClickListener {
            //dialog?.dismiss()
            selectActionListener.invoke(
                ConfirmKey.Rejected,
                dialog,
                bookingTaxiModel?.bookingCode ?: "",
                bookingTaxiModel?.destinationInfo
            )

        }

        binding.actionAcceptMtb.setOnClickListener {
            selectActionListener.invoke(
                ConfirmKey.Accepted,
                dialog,
                bookingTaxiModel?.bookingCode ?: "",
                bookingTaxiModel?.destinationInfo
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap
        googleMap?.let {
            it.uiSettings.isMyLocationButtonEnabled = false
            it.uiSettings.isRotateGesturesEnabled = false
            it.mapType = GoogleMap.MAP_TYPE_NORMAL
            it.isTrafficEnabled = true

            // Add a marker in hotel and move the camera
            val hotel = LatLng(
                bookingTaxiModel?.destinationInfo?.mapLat ?: 0.0,
                bookingTaxiModel?.destinationInfo?.mapLng ?: 0.0
            )
            googleMap.addMarker(
                MarkerOptions().position(hotel)
                    .title(bookingTaxiModel?.destinationInfo?.title ?: bookingTaxiModel?.destinationInfo?.toTitle ?: "---")
            ).showInfoWindow()
            val hasGps = PermissionHelper.hasCOARSEAndFINELocationPermission(fContext)
            if (hasGps) {
                googleMap.isMyLocationEnabled = true
                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { location ->
                        location?.let {
                            val gpsLatLng = LatLng(
                                location.latitude,
                                location.longitude
                            )
                            val destinationLatLng = LatLng(
                                bookingTaxiModel?.destinationInfo?.mapLat ?: 0.0,
                                bookingTaxiModel?.destinationInfo?.mapLng ?: 0.0
                            )
                            val group = LatLngBounds.Builder()
                                .include(gpsLatLng) // LatLgn object1
                                .include(destinationLatLng) // LatLgn object2
                                .build()

                            googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(
                                    group, 100
                                )
                            )
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            fContext,
                            "current gps is null. please enable your permission gps.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }


}