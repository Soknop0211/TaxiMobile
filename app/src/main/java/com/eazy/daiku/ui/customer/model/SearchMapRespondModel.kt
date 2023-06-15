package com.eazy.daiku.ui.customer.model

import com.eazy.daiku.ui.customer.model.Features
import com.google.gson.annotations.SerializedName


data class SearchMapRespondModel(

    @SerializedName("type") var type: String? = null,
    @SerializedName("query") var query: ArrayList<String> = arrayListOf(),
    @SerializedName("features") var features: ArrayList<Features> = arrayListOf(),
    @SerializedName("attribution") var attribution: String? = null,
    @SerializedName("status") var status: Int? = null,

    )