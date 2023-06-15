package com.eazy.daiku.ui.customer.model

import com.example.example.ServiceFees
import com.google.gson.annotations.SerializedName


data class PreviewCheckoutModel(

    @SerializedName("qr_code_url") var qrCodeUrl: String? = null,
    @SerializedName("from_location") var fromLocation: ArrayList<String> = arrayListOf(),
    @SerializedName("to_location") var toLocation: ArrayList<String> = arrayListOf(),
    @SerializedName("kilometre") var kilometre: Double? = null,
    @SerializedName("total_price") var totalPrice: Double? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("service_fees") var serviceFees: ArrayList<ServiceFees> = arrayListOf(),
)