package com.eazy.daiku.ui.customer.model.sear_map

import com.eazy.daiku.ui.customer.model.sear_map.Northeast
import com.eazy.daiku.ui.customer.model.sear_map.Southwest
import com.google.gson.annotations.SerializedName


data class Viewport (

    @SerializedName("northeast" ) var northeast : Northeast? = Northeast(),
    @SerializedName("southwest" ) var southwest : Southwest? = Southwest()

)