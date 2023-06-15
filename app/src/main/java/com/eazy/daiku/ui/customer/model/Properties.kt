package com.eazy.daiku.ui.customer.model

import com.google.gson.annotations.SerializedName


data class Properties (

  @SerializedName("short_code" ) var shortCode : String? = null,
  @SerializedName("wikidata"   ) var wikidata  : String? = null

)