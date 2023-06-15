package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName


data class CustomerHistoryModel(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("code") var code: String? = null,
    @SerializedName("total") var total: Double? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("meta") var meta: CustomerMeta? = CustomerMeta(),
    @SerializedName("service") var service: Service? = Service(),
    @SerializedName("created_at") var createdAt: String? = null,

    )