package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName

data class KycDoc(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("term_id") var termId: Int? = null,
    @SerializedName("term_name") var termName: String? = null,
    @SerializedName("refuse_msg") var refuseMsg: String? = "",
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null,
    @SerializedName("user_id") var userId: Int? = null,
    @SerializedName("plate_number") var plateNumber: String? = null,
    @SerializedName("community_group") var communityTaxi: CommunityTaxiRespond? = null,
    @SerializedName("organization_code") var organizationCode: String? = null,
)