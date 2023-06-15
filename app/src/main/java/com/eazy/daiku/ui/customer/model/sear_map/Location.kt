package com.eazy.daiku.ui.customer.model.sear_map

import com.google.gson.annotations.SerializedName


data class Location (

  @SerializedName("lat" ) var lat : Double? = null,
  @SerializedName("lng" ) var lng : Double? = null

)