package com.eazy.daiku.utility.view_model.user_case.model

data class CreateUserState(
    val organizationCode: Int? = null,
    val firstName: Int? = null,
    val lastName: Int? = null,
    val gender: Int? = null,
    val dob: Int? = null,
    val email: Int? = null,
    val phoneNumber: Int? = null,
    val password: Int? = null,
    val confirmPassword: Int? = null,
    val hasDoneValidate: Boolean? = false
) {

}