package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName

data class Destination(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("map_lat") var mapLat: Double? = null,
    @SerializedName("map_lng") var mapLng: Double? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("from_title") var fromTitle: String? = null,
    @SerializedName("to_title") var toTitle: String? = null,
)