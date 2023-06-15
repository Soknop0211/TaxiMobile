package com.eazy.daiku.data.model.server_model

data class CreateUserRespond(
    val access_token: String?,
    val kyc_doc: KycDoc?,
    val user: User?

)