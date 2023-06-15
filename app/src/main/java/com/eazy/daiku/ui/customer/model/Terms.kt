package com.eazy.daiku.ui.customer.model

import com.google.gson.annotations.SerializedName


data class Terms(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("slug") var slug: String? = null,
    @SerializedName("create_user") var createUser: Int? = null,
    @SerializedName("term_id") var termId: Int? = null,
    @SerializedName("image_id") var imageId: Int? = null,
    @SerializedName("image_full_url") var imageFullUrl: String? = null,
)