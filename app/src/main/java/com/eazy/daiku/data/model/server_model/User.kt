package com.eazy.daiku.data.model.server_model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("first_name") var firstName: String? = null,
    @SerializedName("last_name") var lastName: String? = null,
    @SerializedName("gender") var gender: Int? = null,
    @SerializedName("email") var email: String? = null,
    @SerializedName("active_status") var activeStatus: Int? = null,
    @SerializedName("dark_mode") var darkMode: Int? = null,
    @SerializedName("messenger_color") var messengerColor: String? = null,
    @SerializedName("avatar") var avatar: String? = null,
    @SerializedName("phone") var phone: String? = null,
    @SerializedName("birthday") var birthday: String? = null,
    @SerializedName("country") var country: String? = null,
    @SerializedName("avatar_id") var avatarId: Int? = null,
    @SerializedName("bio") var bio: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null,
    @SerializedName("business_name") var businessName: String? = null,
    @SerializedName("user_name") var userName: String? = null,
    @SerializedName("is_available") var isAvailable: Int? = null,
    @SerializedName("avatar_url") var avatarUrl: String? = null,
    @SerializedName("avatar_thumb_url") var avatarThumbUrl: String? = null,
    @SerializedName("is_employee") var isEmployee: Int? = null,
    @SerializedName("total_balance") var totalBalance: Double? = null,
    @SerializedName("kyc_document") var kycDocument: KycDoc? = KycDoc(),
)