package com.eazy.daiku.data.model

import com.eazy.daiku.utility.enumerable.HomeScreenActionEnum
import java.util.*

class HomeScreenModel(
    val id: Int,
    val name: String,
    val icon: Int,
    var isEnable: Boolean = true,
    val actionEnum: HomeScreenActionEnum,
)