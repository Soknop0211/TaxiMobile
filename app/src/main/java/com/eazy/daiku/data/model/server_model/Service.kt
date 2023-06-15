package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName

data class Service (

  @SerializedName("id"             ) var id           : Int?    = null,
  @SerializedName("title"          ) var title        : String? = null,
  @SerializedName("image_full_url" ) var imageFullUrl : String? = null,
  @SerializedName("slug"           ) var slug         : String? = null

)