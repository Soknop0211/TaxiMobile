package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName


data class CustomerDestination(

    @SerializedName("title") var title: String? = null,
    @SerializedName("from_title") var fromTitle: String? = null,
    @SerializedName("to_title") var toTitle: String? = null,
    @SerializedName("car_lat") var carLat: Double? = null,
    @SerializedName("car_lng") var carLng: Double? = null,
    @SerializedName("map_lat") var mapLat: Double? = null,
    @SerializedName("map_lng") var mapLng: Double? = null,
    @SerializedName("distance") var distance: Double? = null,
    @SerializedName("distance_calculator") var distanceCalculator: CustomerDistanceCalculator? = CustomerDistanceCalculator(),

    )