package com.eazy.daiku.ui.customer.model

import com.google.gson.annotations.SerializedName


data class Context (

  @SerializedName("id"          ) var id         : String? = null,
  @SerializedName("wikidata"    ) var wikidata   : String? = null,
  @SerializedName("short_code"  ) var shortCode  : String? = null,
  @SerializedName("text_en"     ) var textEn     : String? = null,
  @SerializedName("language_en" ) var languageEn : String? = null,
  @SerializedName("text"        ) var text       : String? = null,
  @SerializedName("language"    ) var language   : String? = null

)