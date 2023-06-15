package com.example.example

import com.google.gson.annotations.SerializedName


data class Booking(

    @SerializedName("code") var code: String? = null,
    @SerializedName("from_lat") var fromLat: Double? = null,
    @SerializedName("from_long") var fromLong: Double? = null,
    @SerializedName("to_lat") var toLat: Double? = null,
    @SerializedName("to_long") var toLong: Double? = null,
    @SerializedName("total") var total: Double? = null,
    @SerializedName("device_id") var deviceId: String? = null,
    @SerializedName("device_location") var deviceLocation: String? = null,
    @SerializedName("app_type") var appType: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("destination_info") var destinationInfo: DestinationInfo? = DestinationInfo(),
    @SerializedName("driver_info") var driverInfo: DriverInfo? = DriverInfo(),
    @SerializedName("qr_code_url") var qrCodeUrl: String? = null


)