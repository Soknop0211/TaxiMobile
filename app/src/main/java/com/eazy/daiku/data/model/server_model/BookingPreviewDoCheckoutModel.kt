package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName


data class BookingPreviewDoCheckoutModel(

    @SerializedName("drivers") var drivers: ArrayList<Drivers> = arrayListOf(),
    @SerializedName("from_title") var fromTitle: String? = null,
    @SerializedName("to_title") var toTitle: String? = null,
    @SerializedName("qr_code_url") var qrCodeUrl: String? = null,
    @SerializedName("from_location") var fromLocation: ArrayList<Double>? = null,
    @SerializedName("to_location") var toLocation: ArrayList<Double>? = null,
    @SerializedName("kilometre") var kilometre: Double? = null,
    @SerializedName("vehicle") var vehicle: ArrayList<Vehicle> = arrayListOf(),

    )