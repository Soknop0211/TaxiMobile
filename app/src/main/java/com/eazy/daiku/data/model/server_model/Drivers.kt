package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName


data class Drivers (

  @SerializedName("driver_id" ) var driverId : Int?    = null,
  @SerializedName("name"      ) var name     : String? = null

)