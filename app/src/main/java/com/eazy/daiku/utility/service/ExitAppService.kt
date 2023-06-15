package com.eazy.daiku.utility.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ExitAppService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        println("########## onTaskRemoved called");
        super.onTaskRemoved(rootIntent)
        //do something you want before app closes.
        //stop service
        this.stopSelf()
    }

}