package com.trannhat2411999.bongbongchuyenngu

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.util.*

class CaptureService : Service() {

    companion object {
        val ACTION_ENABLE_CAPTURE = "enable_capture"
    }

    private val notificationId = Random().nextInt()

    private val capture = Capture(this)

    // ... snip ...

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                ACTION_ENABLE_CAPTURE -> onEnableCapture()
            }
        }
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun enableCapture() {
        if (MainActivity.projection == null) {
            val intent = Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            onEnableCapture()
        }
    }

    private fun onEnableCapture() {
        MainActivity.projection?.run {
            capture.run(this) {
                capture.stop()
                // save bitmap
            }
        }
    }

    private fun disableCapture() {
        capture.stop()
        MainActivity.projection = null
    }

    override fun onDestroy() {
        super.onDestroy()
        disableCapture()
    }
}