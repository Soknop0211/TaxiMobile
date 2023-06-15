package com.eazy.daiku.utility

object Constants {

    const val EAZYTAXI_PREF_NAME: String = "EAZYTAXI_PREF_NAME"

    const val pretty_gson: String = "eazyLog"
    const val userDetailKey: String = "userDetailKey"
    const val biometricKey: String = "biometricKey"
    const val phoneNumberKey: String = "phoneNumberKey"
    const val passWordKey : String = "passWordKey"
    const val tripStateKey: String = "tripStateKey"
    const val saveListAccountNumberBank: String = "saveABAAccountNumberBank"
    const val saveFirstBankAccount: String="saveFirstBankAccount"
    const val saveListSearchHistoryMap:String="saveListSearchHistoryMap"

    const val startTrip: Int = 1
    const val endTrip: Int = 0

    object Token {
        const val API_TOKEN: String = "api_token"
    }

    object PhoneConfig {
        const val PHONE_MIN_LENGTH = 9
        const val PHONE_MAX_LENGTH = 19 //last one 12
        const val PHONE_CONFIRMATION_CODE = 6
    }

}