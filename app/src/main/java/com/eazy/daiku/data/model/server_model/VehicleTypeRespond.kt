package com.eazy.daiku.data.model.server_model

data class VehicleTypeRespond(
    val id: Int,
    val name: String?,
    var selectedVehicle: Int = -1,
    val image: String?,
)