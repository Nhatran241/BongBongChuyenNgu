package com.trannhat2411999.bongbongchuyenngu

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.util.Log
import java.nio.ByteBuffer

class Capture(val context: Context) : ImageReader.OnImageAvailableListener {

    companion object {
    }

    private var display: VirtualDisplay? = null
    private var onCaptureListener: ((Bitmap) -> Unit)? = null
    private lateinit var reader : ImageReader

    fun run(mediaProjection: MediaProjection, onCaptureListener: (Bitmap) -> Unit) {
        Log.d("nhatnhat","oke")
        this.onCaptureListener = onCaptureListener
        if (display == null) {
            display = createDisplay(mediaProjection)
        }else {
            captureImage(reader)
        }
    }

    private fun createDisplay(mediaProjection: MediaProjection): VirtualDisplay {
        context.resources.displayMetrics.run {
            val maxImages = 1
            reader = ImageReader.newInstance(
                    widthPixels, heightPixels, PixelFormat.RGBA_8888, maxImages)
            reader.setOnImageAvailableListener(this@Capture, null)
            val display = mediaProjection.createVirtualDisplay(
                    "Capture Display", widthPixels, heightPixels, densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    reader.surface, null, null)
            return display
        }
    }

    override fun onImageAvailable(reader: ImageReader) {
        Log.d("nhatnhat","imageAvailable")
        if (display != null) {
            Log.d("nhatnhat","!null")
            onCaptureListener?.invoke(captureImage(reader))
            stop()
        }
    }



    private fun captureImage(reader: ImageReader): Bitmap {
        val image = reader.acquireLatestImage()
        val planes: Array<Image.Plane> = image.getPlanes()
        val buffer: ByteBuffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val bitmap = Bitmap.createBitmap(
            rowStride / pixelStride, 1024, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        image.close()
        return bitmap
    }

    fun stop() {
        display?.release()
        display = null
        onCaptureListener = null
    }
}