package com.eazy.daiku.utility.other

import android.os.Build

class DeviceConfig {
    /**
     * @return The device's serial number, visible to the user in `Settings > About phone/tablet/device > Status
     * > Serial number`, or `null` if the serial number couldn't be found
     */
    companion object{
        fun getSerialNumber(): String? {
            var serialNumber: String?
            try {
                val c = Class.forName("android.os.SystemProperties")
                val get = c.getMethod("get", String::class.java)

                // (?) Lenovo Tab (https://stackoverflow.com/a/34819027/1276306)
                serialNumber = get.invoke(c, "gsm.sn1") as String
                if (serialNumber == "") // Samsung Galaxy S5 (SM-G900F) : 6.0.1
                // Samsung Galaxy S6 (SM-G920F) : 7.0
                // Samsung Galaxy Tab 4 (SM-T530) : 5.0.2
                // (?) Samsung Galaxy Tab 2 (https://gist.github.com/jgold6/f46b1c049a1ee94fdb52)
                    serialNumber = get.invoke(c, "ril.serialnumber") as String
                if (serialNumber == "") // Archos 133 Oxygen : 6.0.1
                // Google Nexus 5 : 6.0.1
                // Hannspree HANNSPAD 13.3" TITAN 2 (HSG1351) : 5.1.1
                // Honor 5C (NEM-L51) : 7.0
                // Honor 5X (KIW-L21) : 6.0.1
                // Huawei M2 (M2-801w) : 5.1.1
                // (?) HTC Nexus One : 2.3.4 (https://gist.github.com/tetsu-koba/992373)
                    serialNumber = get.invoke(c, "ro.serialno") as String
                if (serialNumber == "") // (?) Samsung Galaxy Tab 3 (https://stackoverflow.com/a/27274950/1276306)
                    serialNumber = get.invoke(c, "sys.serialnumber") as String
                if (serialNumber == "") // Archos 133 Oxygen : 6.0.1
                // Hannspree HANNSPAD 13.3" TITAN 2 (HSG1351) : 5.1.1
                // Honor 9 Lite (LLD-L31) : 8.0
                // Xiaomi Mi 8 (M1803E1A) : 8.1.0
                    serialNumber = Build.SERIAL

                // If none of the methods above worked
                if (serialNumber == Build.UNKNOWN) serialNumber = null
            } catch (e: Exception) {
                e.printStackTrace()
                serialNumber = null
            }
            return serialNumber
        }
    }
}