package com.eazy.daiku.data.model.server_model

data class Role(
    val created_at: String,
    val guard_name: String,
    val id: Int,
    val lang: Any,
    val name: String,
    val origin_id: Any,
    val pivot: Pivot,
    val updated_at: String
)