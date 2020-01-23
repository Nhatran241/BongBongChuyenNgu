package com.trannhat2411999.bongbongchuyenngu

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mediaProjectionManager = getSystemService(Service.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        checkChatHeadPermission()
    }

    companion object {
        private const val REQUEST_CAPTURE = 1

        var projection: MediaProjection? = null
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CAPTURE) {
            if (resultCode == RESULT_OK) {
                projection = mediaProjectionManager.getMediaProjection(resultCode, data!!)
                val intent = Intent(this, BongBongService::class.java)
                    .setAction(CaptureService.ACTION_ENABLE_CAPTURE)
                startForegroundService(intent)
            } else {
                projection = null
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        }else if (requestCode == 21){
            if (resultCode == RESULT_OK) {

                startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CAPTURE)
            }
        }
        finish()
    }

    private lateinit var mediaProjectionManager: MediaProjectionManager


    private fun checkChatHeadPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 21)
        }else {

            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CAPTURE)
        }
    }

}
