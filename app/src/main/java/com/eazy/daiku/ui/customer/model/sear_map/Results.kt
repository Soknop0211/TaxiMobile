package com.eazy.daiku.ui.customer.model.sear_map

import com.eazy.daiku.ui.customer.model.sear_map.Geometry
import com.eazy.daiku.ui.customer.model.sear_map.OpeningHours
import com.eazy.daiku.ui.customer.model.sear_map.PlusCode
import com.google.gson.annotations.SerializedName


data class Results (

    @SerializedName("business_status"       ) var businessStatus      : String?           = null,
    @SerializedName("formatted_address"     ) var formattedAddress    : String?           = null,
    @SerializedName("geometry"              ) var geometry            : Geometry?         = Geometry(),
    @SerializedName("icon"                  ) var icon                : String?           = null,
    @SerializedName("icon_background_color" ) var iconBackgroundColor : String?           = null,
    @SerializedName("icon_mask_base_uri"    ) var iconMaskBaseUri     : String?           = null,
    @SerializedName("name"                  ) var name                : String?           = null,
   // @SerializedName("opening_hours"         ) var openingHours        : OpeningHours?     = OpeningHours(),
    @SerializedName("place_id"              ) var placeId             : String?           = null,
    @SerializedName("plus_code"             ) var plusCode            : PlusCode?         = PlusCode(),
    @SerializedName("rating"                ) var rating              : Double?           = null,
    @SerializedName("reference"             ) var reference           : String?           = null,
    @SerializedName("types"                 ) var types               : ArrayList<String> = arrayListOf(),
    @SerializedName("user_ratings_total"    ) var userRatingsTotal    : Int?              = null

)