package com.example.smartdrugcart

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.smartdrugcart.databinding.ActivityMainBinding
import com.example.smartdrugcart.databinding.ActivityScannerBinding
import com.example.smartdrugcart.helpers.CameraXViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class ScannerActivity : AppCompatActivity() {

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var cameraSelector: CameraSelector? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private val binding: ActivityScannerBinding by lazy {
        ActivityScannerBinding.inflate(layoutInflater)//
        
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.setFlags(1024, 1024)

        if (isCameraPermissionGranted()) {
            // startCamera
            setupCamera()
            Log.e(TAG, "setupCamera")

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_CAMERA_REQUEST
            )
        }

        binding.cancelTV.setOnClickListener {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setupCamera() {

        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(
            CameraXViewModel::class.java)
            .processCameraProvider
            .observe(this, Observer { provider: ProcessCameraProvider?->

                cameraProvider = provider
                if (isCameraPermissionGranted()) {
                    bindPreviewUseCase()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        PERMISSION_CAMERA_REQUEST
                    )
                }
            })
    }

    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private val screenAspectRatio = 1
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("WrongConstant")
    private fun bindPreviewUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }

        previewUseCase = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(binding.previewView!!.display.rotation)
            .build()
        previewUseCase!!.setSurfaceProvider(binding.previewView.surfaceProvider)

        analysisUseCase = ImageAnalysis.Builder()
            .setImageQueueDepth(STRATEGY_KEEP_ONLY_LATEST)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(binding.previewView!!.display.rotation)
            .build()
        analysisUseCase!!.setAnalyzer(ContextCompat.getMainExecutor(this), ImageAnalysis.Analyzer { imageProxy ->
            Log.e(TAG, "setAnalyzer")
            processImageProxy(imageProxy)
        })


        try {
            cameraProvider!!.bindToLifecycle(this, cameraSelector!!, previewUseCase, analysisUseCase)

        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message.toString())
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message.toString())
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(imageProxy: ImageProxy) {

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_ALL_FORMATS
            )
            .build()

//        Barcode.FORMAT_CODE_39,
//        Barcode.FORMAT_CODABAR,
//        Barcode.FORMAT_QR_CODE,

        val scanner = BarcodeScanning.getClient(options)
        val inputImage = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val bounds = barcode.boundingBox
                    val corners = barcode.cornerPoints

                    val rawValue = barcode.rawValue
                    val valueType = barcode.valueType
                    val format = barcode.format

                    // See API reference for complete list of supported types
                    //Log.i("eojfowegnwe", "format: ${barcode.format}")

                    if(rawValue == null){
                        return@addOnSuccessListener
                    }

                    Log.i("eojfowegnwe", "valueType: $valueType")
                    Log.i("eojfowegnwe", "rawValue: $rawValue")

                    when (valueType) {
                        Barcode.TYPE_WIFI -> {
                            val ssid = barcode.wifi!!.ssid
                            val password = barcode.wifi!!.password
                            val type = barcode.wifi!!.encryptionType
                            Toast.makeText(this, "QR Code type wifi.", Toast.LENGTH_SHORT).show()
                        }
                        Barcode.TYPE_URL -> {
                            val title = barcode.url!!.title
                            val url = barcode.url!!.url

                            Toast.makeText(this, "QR Code type Url.", Toast.LENGTH_SHORT).show()
                        }
                        Barcode.TYPE_TEXT ->{

                            when(format){
                                Barcode.FORMAT_CODE_39->{

                                    val data = Intent()
                                    data.putExtra("SCAN_RESULT", rawValue)
                                    setResult(RESULT_OK, data)
                                    finish()
                                }
                                else->{
                                    val data = Intent()
                                    data.putExtra("SCAN_RESULT", rawValue)
                                    setResult(RESULT_OK, data)
                                    finish()
                                }
                            }

                        }
                    }
                }

            }
            .addOnFailureListener {
                Log.e(TAG, it.message.toString())
            }.addOnCompleteListener {
                imageProxy.close()
            }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_CAMERA_REQUEST) {
            if (isCameraPermissionGranted()) {
                // start camera
                setupCamera()
            } else {
                Log.e(TAG, "no camera permission")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            baseContext,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val TAG = "eojfowegnwe"
        private const val PERMISSION_CAMERA_REQUEST = 1
    }

    private fun scanBarcodes(image: InputImage) {

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_CODABAR,
            )
            .build()

        val scanner = BarcodeScanning.getClient()

        // [START run_detector]
        val result = scanner.process(image)
            .addOnSuccessListener { barcodes ->
                // Task completed successfully
                // [START_EXCLUDE]
                // [START get_barcodes]
                for (barcode in barcodes) {
                    val bounds = barcode.boundingBox
                    val corners = barcode.cornerPoints

                    val rawValue = barcode.rawValue

                    val valueType = barcode.valueType
                    // See API reference for complete list of supported types
                    Log.i("eojfowegnwe", "valueType: $valueType")
                    Log.i("eojfowegnwe", "rawValue: $rawValue")
                    when (valueType) {
                        Barcode.TYPE_WIFI -> {
                            val ssid = barcode.wifi!!.ssid
                            val password = barcode.wifi!!.password
                            val type = barcode.wifi!!.encryptionType
                        }
                        Barcode.TYPE_URL -> {
                            val title = barcode.url!!.title
                            val url = barcode.url!!.url
                        }
                        Barcode.TYPE_TEXT ->{


                        }
                    }
                }
                // [END get_barcodes]
                // [END_EXCLUDE]
            }
            .addOnFailureListener {
                // Task failed with an exception
                Log.i("eojfowegnwe", "addOnFailureListener: ${it.message}")
                // ...
            }
        // [END run_detector]
    }
}