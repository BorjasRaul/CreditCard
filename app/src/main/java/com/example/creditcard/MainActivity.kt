package com.example.creditcard

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : AppCompatActivity() {
    private val MY_PERMISSIONS_REQUEST_CAMERA: Int = 101
    private lateinit var mCameraSource: CameraSource
    private lateinit var textRecognizer: TextRecognizer
    private lateinit var new:String

    private val tag: String? = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

            requestForPermission()
        textRecognizer = TextRecognizer.Builder(this).build()
        if (!textRecognizer.isOperational) {
            Toast.makeText(this, "Dependencies are not loaded yet...please try after few moment!!", Toast.LENGTH_SHORT)
                    .show()
            Log.e(tag, "Dependencies are downloading....try after few moment")
            return
        }
        mCameraSource = CameraSource.Builder(applicationContext, textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(3280, 4524)
                .setAutoFocusEnabled(true)
                .setRequestedFps(0.40f)
                .build()

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceDestroyed(p0: SurfaceHolder) {
                mCameraSource.stop()
            }

            @SuppressLint("MissingPermission")
            override fun surfaceCreated(p0: SurfaceHolder) {
                try {
                    if (isCameraPermissionGranted()) {
                        mCameraSource.start(surfaceView.holder)
                    } else {
                        requestForPermission()
                    }
                } catch (e: Exception) {
                    toast("Error:" + e.message)
                }
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
            }
        }
        )

        textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                lateinit var rsult:String
                if(detections != null && detections.detectedItems.isNotEmpty()){
                    new = ""
                    val findtext = detections.detectedItems
                    tv_result.post {
                        val stringBuilder = StringBuilder()
                        for (i in 0 until findtext.size()) {
                            val item = findtext.valueAt(i)
                            stringBuilder.append(item.value)
                            stringBuilder.append("\n")
                        }

                        rsult=stringBuilder.toString()
                        val re=Regex("[^0-9]")
                        rsult=re.replace(rsult,"")
                        if(rsult.length<=16){
                               tv_result.text = rsult
                            new =rsult
                    }
                }
                }else{
                    tv_result.text = " "
                }
            }
        })
        btCalculate.setOnClickListener{
            if(new.isNullOrEmpty()){
                Toast.makeText(this@MainActivity,"Vuelva a intentar",Toast.LENGTH_LONG).show()
            }else {
                newres.setText(new)
            }
        }

    }

    private fun requestForPermission() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.CAMERA
                )
                != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this@MainActivity,
                            Manifest.permission.CAMERA
                    )
            ) {
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(
                                Manifest.permission.CAMERA
                        ),
                        MY_PERMISSIONS_REQUEST_CAMERA
                )
            }
        }
    }

    private fun isCameraPermissionGranted(): Boolean {

        return ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    }

    //method for toast
    fun toast(text: String) {

        Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()

    }

    //for handling permissions
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    requestForPermission()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }
}