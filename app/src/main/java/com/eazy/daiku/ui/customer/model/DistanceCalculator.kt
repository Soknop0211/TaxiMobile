package com.example.example

import com.google.gson.annotations.SerializedName


data class DistanceCalculator (

  @SerializedName("end"         ) var end        : String? = null,
  @SerializedName("price"       ) var price      : String? = null,
  @SerializedName("start"       ) var start      : String? = null,
  @SerializedName("extra_price" ) var extraPrice : String? = null

)