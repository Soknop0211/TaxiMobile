package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName

data class DestinationInfo(
    @SerializedName("title") var title: String? = null,
    @SerializedName("to_title") var toTitle: String? = null,
    @SerializedName("total") var total: Double? = null,
    @SerializedName("car_lat") var carLat: String? = null,
    @SerializedName("car_lng") var carLng: String? = null,
    @SerializedName("map_lat") var mapLat: Double? = null,
    @SerializedName("map_lng") var mapLng: Double? = null,
    @SerializedName("vehicle") var vehicle: String? = null,
    @SerializedName("distance") var distance: Double? = null,
    @SerializedName("distance_calculator") var distanceCalculator: DistanceCalculator? = null
)