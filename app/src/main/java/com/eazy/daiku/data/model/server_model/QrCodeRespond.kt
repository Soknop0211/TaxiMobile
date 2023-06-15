package com.eazy.daiku.data.model.server_model

import com.example.example.DriverInfo

data class QrCodeRespond(
    var title: String? = null,
    var from_title: String? = null,
    var to_title: String? = null,
    val url: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var distance: Double? = null,
    var total_amount: Double? = null,
    val code: String? = null,
    val status: String? = null,
    var driverInfo: DriverInfo? = null,
    var urlQrcode: String? = null,
    var vehicle: String? = null
): java.io.Serializable

