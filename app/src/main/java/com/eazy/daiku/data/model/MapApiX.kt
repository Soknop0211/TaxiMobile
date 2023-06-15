package com.eazy.daiku.data.model

import java.io.Serializable

data class MapApiX(
    val paths: ArrayList<Path>
):Serializable

data class Path(
    val ascend: Double,
    val descend: Double,
    val distance: Double,
    val legs: ArrayList<Any>,
    val points: String,
    val points_encoded: Boolean,
    val snapped_waypoints: String,
    val transfers: Int,
    val weight: Double,
    val bbox : List<Double>,
    val street_name:String
):Serializable
