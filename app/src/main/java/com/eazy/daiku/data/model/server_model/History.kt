package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName

data class History(
    @SerializedName("code") var code: String? = null,
    @SerializedName("destination") var destination: Destination? = Destination(),
    @SerializedName("destination_id") var destinationId: Int? = null,
    @SerializedName("destination_type") var destinationType: String? = null,
    @SerializedName("distance") var distance: Double? = null,
    @SerializedName("end_time") var endTime: String? = null,
    @SerializedName("id") var id: Int? = null,
    @SerializedName("start_time") var startTime: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("total") var total: Double? = null
)
