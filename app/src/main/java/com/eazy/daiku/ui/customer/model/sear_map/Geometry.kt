package com.eazy.daiku.ui.customer.model.sear_map

import com.google.gson.annotations.SerializedName


data class Geometry (

  @SerializedName("location" ) var location : Location? = Location(),
  @SerializedName("viewport" ) var viewport : Viewport? = Viewport()

)