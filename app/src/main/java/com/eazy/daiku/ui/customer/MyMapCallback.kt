package com.eazy.daiku.ui.customer

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng

public interface MyMapCallback {
    fun onConfirmed(name: String?, address: String?, latLng: LatLng?, bitmap: Bitmap?)
}