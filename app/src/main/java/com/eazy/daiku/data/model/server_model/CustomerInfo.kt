package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName

data class CustomerInfo(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("first_name") var firstName: String? = null,
    @SerializedName("last_name") var lastName: String? = null,
    @SerializedName("gender") var gender: Int? = null,
    @SerializedName("email") var email: String? = null,
    @SerializedName("phone") var phone: String? = null,
)