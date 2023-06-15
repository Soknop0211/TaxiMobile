package com.eazy.daiku.data.remote

import com.eazy.daiku.data.model.ListAllBankAccountModel
import com.eazy.daiku.data.model.VerifyBankAccountModel
import com.eazy.daiku.data.model.WithdrawMoneyRespondModel
import com.eazy.daiku.data.model.base.ApiResWraper
import com.eazy.daiku.data.model.server_model.*
import com.eazy.daiku.di.module.NetworkModule
import com.eazy.daiku.ui.customer.model.ListKioskModel
import com.eazy.daiku.ui.customer.model.PreviewCheckoutModel
import com.eazy.daiku.ui.customer.model.WebPayRespondModel
import com.eazy.daiku.ui.customer.model.sear_map.SearchMapModel
import com.example.example.BookingProcessingModel
import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.Call
import retrofit2.http.*


interface EazyTaxiApi {

    //login
    @POST("api/auth/login")
    suspend fun login(@Body map: HashMap<String, Any>): ApiResWraper<JsonElement>

    //login
    @POST("api/auth/logout")
    suspend fun logout(): ApiResWraper<JsonElement>

    //create user
    @POST("api/auth/register")
    suspend fun createUser(@Body map: HashMap<String, Any>): ApiResWraper<CreateUserRespond>

    //user info
    @GET("api/auth/me")
    suspend fun userInfo(): ApiResWraper<User>

    //get vehicle type
    @GET("api/get-terms")
    suspend fun fetchVehicleType(
        @Query(
            value = "slug",
            encoded = true
        ) type: String,
    ): ApiResWraper<ArrayList<VehicleTypeRespond>>

    @GET("api/booking/{code}")
    suspend fun fetchQrCode(
        @Path(
            value = "code",
            encoded = true
        ) type: String,
    ): ApiResWraper<QrCodeRespond>

    @GET("api/booking/taxi/start/{code}")
    suspend fun startTrip(
        @Path(
            value = "code",
            encoded = true
        ) code: String,
    ): ApiResWraper<QrCodeRespond>

    @GET("api/booking/taxi/end/{code}")
    suspend fun endTrip(
        @Path(
            value = "code",
            encoded = true
        ) code: String,
    ): ApiResWraper<QrCodeRespond>

    @GET("api/user/booking/processing")
    suspend fun fetchTripProcessing(): ApiResWraper<QrCodeRespond>

    //user info
    @POST("api/user/set-available-status")
    suspend fun updateAvailableTaxi(@Body map: HashMap<String, Any>): ApiResWraper<User>

    @POST("api/auth/resubmit-kyc")
    suspend fun reUploadKycDoc(@Body map: HashMap<String, Any>): ApiResWraper<User>

    @GET("api/wallet/getTransaction")
    suspend fun fetchTransaction(@QueryMap map: HashMap<String, Any>): ApiResWraper<TransactionRespond>

    @GET("api/booking/get/history")
    suspend fun fetchHistory(@QueryMap map: HashMap<String, Any>): ApiResWraper<List<History>>

    @GET("api/customers/booking/history")
    suspend fun submitCustomerHistory(@QueryMap map: HashMap<String, Any>): ApiResWraper<List<CustomerHistoryModel>>

    @POST("api/user/withdraw")
    suspend fun submitWithdraw(@Body map: HashMap<String, Any>): ApiResWraper<WithdrawMoneyRespondModel>

    @POST("api/v2/user/withdraw")
    suspend fun userSubmitWithdraw(@Body map: HashMap<String, Any>): ApiResWraper<WithdrawMoneyRespondModel>

    @POST("api/auth/change-password")
    suspend fun submitChangePwd(@Body map: HashMap<String, Any>): ApiResWraper<JsonElement>

    @POST("api/auth/requestOTPForgotPassword")
    suspend fun submitRequestOtp(@Body map: HashMap<String, Any>): ApiResWraper<JsonElement>

    @POST("api/auth/verifyOTPForgotPassword")
    suspend fun submitVerifyOtp(@Body map: HashMap<String, Any>): ApiResWraper<JsonElement>

    @POST("api/auth/changePasswordByOTP")
    suspend fun submitChangePasswordByOtp(@Body map: HashMap<String, Any>): ApiResWraper<JsonElement>

    @GET("api/community/group/user")
    suspend fun fetchCommunityTaxi(@QueryMap map: HashMap<String, Any>): ApiResWraper<List<CommunityTaxiRespond>>

    @POST("api/user/verify/bank-account")
    suspend fun verifyBankAccount(@Body map: HashMap<String, Any>): ApiResWraper<VerifyBankAccountModel>

    @POST("api/auth/me")
    suspend fun updateUserInfo(@Body map: HashMap<String, Any>): ApiResWraper<User>

    @POST("/api/booking/preview/doCheckout")
    suspend fun submitReviewBooking(@Body map: HashMap<String, Any>): ApiResWraper<JsonElement>

    @GET("api/map/v2/searchMap")
    suspend fun submitSearchMap(@Query(value = "search") search: String): SearchMapModel

    @GET("api/map/v2/searchMapLocal")
    suspend fun submitSearchMapLocal(@Query(value = "search") search: String): SearchMapModel

    @POST("api/auth/registerAsCustomer")
    suspend fun submitCreateUserCustomer(@Body map: HashMap<String, Any>): ApiResWraper<CreateUserRespond>

    @GET("api/list-kiosk")
    suspend fun submitSearchLocationKioskNearBy(): ApiResWraper<ListKioskModel>

    @GET("api/car/search")
    suspend fun submitGetTaxiDriver(@QueryMap map: HashMap<String, Any>): ApiResWraper<JsonElement>

    @POST("api/booking/preview/doCheckout")
    suspend fun submitCheckoutTaxi(@Body body: HashMap<String, Any>): ApiResWraper<PreviewCheckoutModel>

    @GET("api/list-kiosk")
    suspend fun submitListAllLocationKiosk(@Query(value = "all_device") allDevice: Boolean): ApiResWraper<List<ListKioskModel>>

    @POST("api/v2/booking/doCheckoutTaxi")
    suspend fun submitCheckout(@Body body: HashMap<String, Any>): ApiResWraper<WebPayRespondModel>

    @GET("api/user/list-bank")
    suspend fun submitListAllBank(): ApiResWraper<List<ListAllBankAccountModel>>

    @POST("api/v2/booking/preview/doCheckout")
    suspend fun submitBookingPreviewDoCheckout(@Body map: HashMap<String, Any>): ApiResWraper<BookingPreviewDoCheckoutModel>

    @GET("api/user/customer/booking/processing")
    suspend fun submitBookingProcessing(): ApiResWraper<BookingProcessingModel>

    @POST("api/user/customer/booking/cancelled/{code}")
    suspend fun submitCancelBookingTaxi(
        @Path(
            value = "code",
            encoded = true
        ) code: String,
    ): ApiResWraper<JsonElement>

//polyline
    @GET("api/1/route")
    fun polylineApi(
        @Query(value = "point") latLngA:String,
        @Query(value = "point") latLngB:String,
        @Query(value = "vehicle") vehicle: String,
        @Query(value = "points_encoded") points_encoded: Boolean,
        @Query(value = "key") key: String
    ): Call<JsonElement>
}
