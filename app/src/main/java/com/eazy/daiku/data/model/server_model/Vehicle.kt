package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName


data class Vehicle(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("price") var price: Double? = null,
    @SerializedName("image") var image: String? = null,
    @SerializedName("total_before_fee") var totalBeforeFee: Double? = null,

    )