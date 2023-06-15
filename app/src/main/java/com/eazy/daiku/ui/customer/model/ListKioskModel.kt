package com.eazy.daiku.ui.customer.model

import com.google.gson.annotations.SerializedName

data class ListKioskModel (
  @SerializedName("id"            ) var id          : Int?             = null,
  @SerializedName("device_id"     ) var deviceId    : String?          = null,
  @SerializedName("location"      ) var location    : String?          = null,
  @SerializedName("map_lat"       ) var mapLat      : Double?          = null,
  @SerializedName("map_lng"       ) var mapLng      : Double?          = null,
  @SerializedName("kiosk_user_id" ) var kioskUserId : Int?             = null,
  @SerializedName("distance"      ) var distance    : Double?          = null,
  @SerializedName("terms"         ) var terms       : ArrayList<Terms> = arrayListOf()
)