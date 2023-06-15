package com.eazy.daiku.ui.customer.model

import com.google.gson.annotations.SerializedName


data class Features (

  @SerializedName("id"            ) var id          : String?            = null,
  @SerializedName("type"          ) var type        : String?            = null,
  @SerializedName("place_type"    ) var placeType   : ArrayList<String>  = arrayListOf(),
  @SerializedName("relevance"     ) var relevance   : Int?               = null,
  @SerializedName("properties"    ) var properties  : Properties?        = Properties(),
  @SerializedName("text_en"       ) var textEn      : String?            = null,
  @SerializedName("language_en"   ) var languageEn  : String?            = null,
  @SerializedName("place_name_en" ) var placeNameEn : String?            = null,
  @SerializedName("text"          ) var text        : String?            = null,
  @SerializedName("language"      ) var language    : String?            = null,
  @SerializedName("place_name"    ) var placeName   : String?            = null,
  @SerializedName("bbox"          ) var bbox        : ArrayList<Double>  = arrayListOf(),
  @SerializedName("center"        ) var center      : ArrayList<Double>  = arrayListOf(),
  @SerializedName("geometry"      ) var geometry    : Geometry?          = Geometry(),
  @SerializedName("context"       ) var context     : ArrayList<Context> = arrayListOf()

)