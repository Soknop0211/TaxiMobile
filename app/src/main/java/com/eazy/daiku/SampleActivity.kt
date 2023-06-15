package com.eazy.daiku

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.eazy.daiku.data.remote.EazyTaxiApi
import com.eazy.daiku.utility.other.AppLOGG

class SampleActivity : AppCompatActivity() {

    private var handler: Handler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        handler = Handler(Looper.getMainLooper())
        handler?.postDelayed(runnableCode, 10000)
    }

    private val runnableCode = object : Runnable {
        override fun run() {
            AppLOGG.d(
                "submitLocationParseServer_",
                "timer working"
            )
            handler?.postDelayed(this, 10000)//60000milli = 1minute
        }
    }

}