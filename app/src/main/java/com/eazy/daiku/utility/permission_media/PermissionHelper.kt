package com.eazy.daiku.utility.permission_media

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.eazy.daiku.utility.BetterActivityResult
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.base.BaseCoreActivity
import java.util.concurrent.atomic.AtomicInteger

object PermissionHelper {


    /**request all permission not available*/
    fun requestCameraAndWriteExternalStorageCamera(
        context: Context,
        permissionListenerCallBack: (Boolean) -> Unit
    ) {
        val camera = Manifest.permission.CAMERA
        val writeStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE
        requestMultiPermission(
            context, arrayOf(camera, writeStorage),
            permissionListenerCallBack
        )
    }

    fun requestMultiPermission(
        context: Context,
        param: Array<String>, callBack: (Boolean) -> Unit
    ) {
        val sizeOfPermissionRequest = AtomicInteger()
        checkedRequestMultiPermission(
            context,
            param,
            object : BetterActivityResult.OnActivityResult<Map<String, Boolean>> {
                override fun onActivityResult(result: Map<String, Boolean>) {
                    result.forEach { (_, hasGranted) ->
                        if (hasGranted) {
                            sizeOfPermissionRequest.getAndIncrement()
                        }
                    }
                    val hasPermission = sizeOfPermissionRequest.toInt() == param.size
                    callBack.invoke(hasPermission)
                }

            }
        )
    }

    private fun checkedRequestMultiPermission(
        activity: Context,
        param: Array<String>,
        activityResult: BetterActivityResult.OnActivityResult<Map<String, Boolean>>
    ) {
        if (activity is BaseCoreActivity) {
            val baseCoreActivity: BaseCoreActivity = activity
            baseCoreActivity.activityMultiPermission.launch(param, activityResult)
        } else {
            Toast.makeText(
                activity,
                "Please checking your activity need to extend from BaseCoreActivity...!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    /**checking all permission are available*/

    fun hasCameraAndExternalStoragePermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    fun hasExternalStoragePermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun hasCOARSEAndFINELocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasDeviceGpsAndNetwork(activity: Activity): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnable = false
        var networkEnable = false
        gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        networkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return gpsEnable && networkEnable
    }
}


