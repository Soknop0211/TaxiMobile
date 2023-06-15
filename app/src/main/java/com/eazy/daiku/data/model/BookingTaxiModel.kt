package com.eazy.daiku.data.model

import com.eazy.daiku.data.model.server_model.CustomerInfo
import com.eazy.daiku.data.model.server_model.DestinationInfo
import com.parse.ParseObject

data class BookingTaxiModel(
    var bookingCode: String? = null,
    var customerInfo: CustomerInfo? = null,
    var destinationInfo: DestinationInfo? = null,
    var bookingParseObj: ParseObject? = null
)