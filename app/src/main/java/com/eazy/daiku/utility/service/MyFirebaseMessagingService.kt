package com.eazy.daiku.utility.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.R
import com.eazy.daiku.ui.splash_screen.SplashScreenActivity
import com.eazy.daiku.ui.verification_pin_code.VerificationPinActivity
import com.eazy.daiku.utility.Config
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.GsonConverterHelper
import com.eazy.daiku.utility.other.AppLOGG
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import java.util.*


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var builder: Notification.Builder

    companion object {
        const val channelId = BuildConfig.APPLICATION_ID + "Chanel_id"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        AppLOGG.d("MyFirebaseMessagingService", Gson().toJson(message) + "\n code--> ")
        sendNotification(message)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        EazyTaxiHelper.setDeviceTokenPreference(
            this,
            Config.FCM_TOKEN,
            "MyToken"
        )
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
//        val jsonStr =
//            "{\"bundle\":{\"mMap\":{\"google.delivered_priority\":\"high\",\"google.sent_time\":1663043237562,\"google.ttl\":2419200,\"google.original_priority\":\"high\",\"push_id\":\"HB5ylKGA64\",\"data\":\"{\\\"alert\\\":\\\"Hello\\\"}\",\"from\":\"1079358092157\",\"time\":\"2022-09-13T04:27:17.531Z\",\"google.message_id\":\"0:1663043237583710%91315a72f9fd7ecd\",\"google.c.sender.id\":\"1079358092157\"}}}"
        //val contentTitle = remoteMessage.notification?.title ?: "N/A"


        var contentBody = remoteMessage.notification?.body ?: "N/A"
        var code = ""

        try {
            val remoteMsgHm = HashMap(remoteMessage.data)
            if (remoteMsgHm.containsKey("data")) {
                val data = remoteMsgHm["data"]
                val dataJsonObject =
                    GsonConverterHelper.getJsonObjectToGenericClass<JsonObject>(data)
                contentBody = dataJsonObject.getAsJsonPrimitive("alert").asString
                code = dataJsonObject.getAsJsonPrimitive("code").asString
            }
        } catch (ignored: JsonSyntaxException) {
        }


        val intentAcceptOrder = Intent(MyBroadcastReceiver.notificationAcceptOrder)
        intentAcceptOrder.putExtra(
            MyBroadcastReceiver.notificationAcceptOrder, code
        )
        sendBroadcast(intentAcceptOrder)

        // it is a class to notify the user of events that happen.
        // This is how you tell the user that something has happened in the
        // background.
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // pendingIntent is an intent for future use i.e after
        // the notification is clicked, this intent will come into action
        val intent = Intent(this, SplashScreenActivity::class.java)
        intent.putExtra(VerificationPinActivity.bookingCode, code)
        // FLAG_UPDATE_CURRENT specifies that if a previous
        // PendingIntent already exists, then the current one
        // will update it with the latest intent
        // 0 is the request code, using it later with the
        // same method again will get back the same pending
        // intent for future reference
        // intent passed here is to our afterNotification class


        /*val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)*/

        var pendingIntent: PendingIntent? = null
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // RemoteViews are used to use the content of
        // some different layout apart from the current activity layout
        //val contentView = RemoteViews(packageName, R.layout.activity_after_notification)

        // checking if android version is greater than oreo(API 26) or not
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel =
                NotificationChannel(
                    channelId,
                    "Show_Notification",
                    NotificationManager.IMPORTANCE_HIGH
                )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this, channelId)
                /*.setContent(contentView)
                .setContentTitle(contentTitle)*/
                .setContentText(contentBody)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setStyle(Notification.BigTextStyle().bigText(contentBody))
        } else {

            builder = Notification.Builder(this)
                /*.setContent(contentView)
                .setContentTitle(contentTitle)*/
                .setContentText(contentBody)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setStyle(Notification.BigTextStyle().bigText(contentBody))
        }
        val UniqueIntegerNumber = (Date().time / 1000L % Int.MAX_VALUE).toInt()
        notificationManager.notify(UniqueIntegerNumber, builder.build())
    }
}