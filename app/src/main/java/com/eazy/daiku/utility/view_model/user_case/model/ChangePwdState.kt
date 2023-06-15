package com.eazy.daiku.utility.view_model.user_case.model

data class changePwdState(
    val newPwd: Int? = null,
    val confirmPwd: Int? = null,
    val pwdNotMatch: Int? = null,
    val hasDoneValidate: Boolean? = false
)