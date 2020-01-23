package com.trannhat2411999.bongbongchuyenngu

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.*
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.translate.view.*
import kotlinx.android.synthetic.main.translate_small.view.*


class BongBongService : Service() {
    private lateinit var mBongBongView :View
    private lateinit var mTranslateView :View
    private lateinit var mWindowManager: WindowManager
    private val capture = Capture(this)
    var lastAction: Int = -1
    var initialX = 0
    var initialY = 0
    var initialTouchX = 0f
    var initialTouchY = 0f

    private val vietnameseToEngLish = FirebaseTranslatorOptions.Builder()
        .setSourceLanguage(FirebaseTranslateLanguage.VI)
        .setTargetLanguage(FirebaseTranslateLanguage.EN)
        .build()
    private val englishToVietnamese = FirebaseTranslatorOptions.Builder()
        .setSourceLanguage(FirebaseTranslateLanguage.EN)
        .setTargetLanguage(FirebaseTranslateLanguage.VI)
        .build()
    private lateinit var vietnamesetoenglish :FirebaseTranslator




    override fun onCreate() {
        super.onCreate()
        initView()
        initData()
    }

    private fun initData() {

        FirebaseApp.initializeApp(this)

        vietnamesetoenglish = FirebaseNaturalLanguage.getInstance().getTranslator(englishToVietnamese)
        vietnamesetoenglish.downloadModelIfNeeded()
            .addOnSuccessListener {
            }
            .addOnFailureListener { exception ->
            }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                //CaptureService.ACTION_ENABLE_CAPTURE -> onEnableCapture()
            }
        }
        return Service.START_STICKY
    }
    private fun onEnableCapture() {
        MainActivity.projection?.run {
            capture.run(this) {
                capture.stop()
                // save bitmap

            }
        }
    }



    private fun initView() {
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mBongBongView =LayoutInflater.from(this).inflate(R.layout.bongbong, null)
        val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        }
        params.gravity = Gravity.TOP or Gravity.START
        params.windowAnimations = android.R.style.Animation_Toast
        params.x = 0
        params.y = 0
        mWindowManager.addView(mBongBongView, params)

        mTranslateView =LayoutInflater.from(this).inflate(R.layout.translate_small, null)


        ArrayAdapter.createFromResource(
            this,
            R.array.planets_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            mTranslateView.sp_source.adapter = adapter
            mTranslateView.sp_target.adapter = adapter
        }
        val mTranslateParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )
        }
        mTranslateParams.gravity = Gravity.TOP or Gravity.START
        mTranslateParams.windowAnimations = android.R.style.Animation_Toast
        mTranslateParams.x = 0
        mTranslateParams.y = 0

        mTranslateView.btn_tranlate.setOnClickListener {
            vietnamesetoenglish.translate(mTranslateView.ed_input_source.text.toString()).addOnSuccessListener {
                    translatedText ->
                mTranslateView.ed_inputtarget.setText(translatedText)
            }
        }
        mTranslateView.btn_close.setOnClickListener {
            mWindowManager.removeView(mTranslateView)
            mWindowManager.addView(mBongBongView,params)
        }

        mTranslateView.btn_screentranlate.setOnClickListener {
            mTranslateView.visibility=View.GONE
            mWindowManager.updateViewLayout(mTranslateView,mTranslateParams)
            val detector = FirebaseVision.getInstance().cloudTextRecognizer
            MainActivity.projection?.run {
                capture.run(this) {
                    capture.stop()
                    mTranslateView.visibility=View.VISIBLE
                    mWindowManager.updateViewLayout(mTranslateView,mTranslateParams)
                    val myImage = FirebaseVisionImage.fromBitmap(it)
                    detector.processImage(myImage)
                        .addOnSuccessListener { firebaseVisionDocumentText ->
                            vietnamesetoenglish.translate(firebaseVisionDocumentText.text).addOnSuccessListener {
                                    translatedText ->
                                mTranslateView.ed_inputtarget.setText(translatedText)
                            }


                        }
                        .addOnFailureListener { e ->
                            Log.d("nhatnhat", e.toString())

                        }

                }
            }
        }
        /**
        mDichCamera.setOnClickListener {

            val InputStream = getResources().openRawResource(R.drawable.hello_world)

            mTranslateView.visibility=View.GONE
            mWindowManager.updateViewLayout(mTranslateView,mTranslateParams)
                val detector = FirebaseVision.getInstance().cloudTextRecognizer
            MainActivity.projection?.run {
                capture.run(this) {
                    capture.stop()
                    mTranslateView.visibility=View.VISIBLE
                    mWindowManager.updateViewLayout(mTranslateView,mTranslateParams)
                    val myImage = FirebaseVisionImage.fromBitmap(it)
                    detector.processImage(myImage)
                        .addOnSuccessListener { firebaseVisionDocumentText ->
                            vietnamesetoenglish.translate(firebaseVisionDocumentText.text.toString()).addOnSuccessListener {
                                    translatedText ->
                                Log.d("nhatnhat", "asdasd")
                                mKq.setText(translatedText)
                            }


                        }
                        .addOnFailureListener { e ->
                            Log.d("nhatnhat", e.toString())

                        }

                }
            }

            }

        mDich.setOnClickListener {
            vietnamesetoenglish.translate(mInput.text.toString()).addOnSuccessListener {
                translatedText ->
                mKq.setText(translatedText)
            }
        }
        mButton.setOnClickListener {
            mWindowManager.removeView(mTranslateView)
            mWindowManager.addView(mBongBongView,params)
        }
        **/

        initListener(params,mTranslateParams)
        initListener2(mTranslateParams)


    }

    private fun initTranslateBoxView() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.stop()

    }

    private fun initListener(
        params: WindowManager.LayoutParams,
        mTranslateParams: WindowManager.LayoutParams
    ) {
        //Get screen width
        val display = mWindowManager.defaultDisplay
        val screenWith = display.width

        //implement click listener
        mBongBongView.setOnClickListener {
            //TODO  do something
        }

        //implement touch listener
        mBongBongView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //save start position
                    initialX = params.x
                    initialY = params.y

                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    lastAction = event.action
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (lastAction == MotionEvent.ACTION_DOWN) {
                        mTranslateParams.x =params.x
                        mTranslateParams.y =params.y
                        mWindowManager.addView(mTranslateView,mTranslateParams)
                        mWindowManager.removeView(mBongBongView)
                    }

                    lastAction = event.action
                    if (params.x < screenWith / 2) {
                        while (params.x > 0) {
                            params.x -= 10
                            mWindowManager.updateViewLayout(mBongBongView, params)
                        }
                    } else {
                        while (params.x < screenWith) {
                            params.x += 10
                            mWindowManager.updateViewLayout(mBongBongView, params)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    mWindowManager.updateViewLayout(mBongBongView, params)
                    lastAction = event.action
                    true
                }

                else -> false
            }
        }
    }

    private fun initListener2(
        params: WindowManager.LayoutParams
    ) {
        //Get screen width
        val display = mWindowManager.defaultDisplay
        val screenWith = display.width

        //implement click listener
        mTranslateView.setOnClickListener {
            //TODO  do something
        }

        //implement touch listener
        mTranslateView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //save start position
                    initialX = params.x
                    initialY = params.y

                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    lastAction = event.action
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (lastAction == MotionEvent.ACTION_DOWN) {
                    }

                    lastAction = event.action
                    if (params.x < screenWith / 2) {
                        while (params.x > 0) {
                            params.x -= 10
                            mWindowManager.updateViewLayout(mTranslateView, params)
                        }
                    } else {
                        while (params.x < screenWith) {
                            params.x += 10
                            mWindowManager.updateViewLayout(mTranslateView, params)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    mWindowManager.updateViewLayout(mTranslateView, params)
                    lastAction = event.action
                    true
                }

                else -> false
            }
        }
    }

}