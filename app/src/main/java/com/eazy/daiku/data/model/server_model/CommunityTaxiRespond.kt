package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName

data class CommunityTaxiRespond(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("business_name") var businessName: String? = null
)
