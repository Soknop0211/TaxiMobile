package com.example.example

import com.google.gson.annotations.SerializedName


data class BookingProcessingModel (

  @SerializedName("booking" ) var booking : Booking? = Booking()

)