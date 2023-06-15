package com.example.example

import com.google.gson.annotations.SerializedName


data class DriverInfo (

  @SerializedName("id"        ) var id       : Int?    = null,
  @SerializedName("name"      ) var name     : String? = null,
  @SerializedName("phone"     ) var phone    : String? = null,
  @SerializedName("avatar_id" ) var avatarId : String? = null

)