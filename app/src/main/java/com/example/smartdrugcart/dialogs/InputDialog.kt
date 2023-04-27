package com.example.smartdrugcart.dialogs

import android.Manifest
import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import android.view.Window
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smartdrugcart.ScannerActivity
import com.example.smartdrugcart.databinding.DialogInputBinding

class InputDialog(private var activity: Activity, private var barcodeForResult: ActivityResultLauncher<Intent>): Dialog(activity) {

    private var l: ((text: String)->Unit)? = null
    fun setEvent(l: (text: String)->Unit){
        this.l = l
    }

    fun setShowErrorInput(isShow: Boolean){
        when(isShow){
            true->{
                binding.messageTV.visibility = View.VISIBLE
            }
            false->{
                binding.messageTV.visibility = View.GONE
            }
        }
    }

    private val binding: DialogInputBinding by lazy {
        DialogInputBinding.inflate(layoutInflater)
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(true)

        //init
        binding.messageTV.visibility = View.GONE
        binding.inputEDT.requestFocus()

        binding.okTV.setOnClickListener {
            val text = binding.inputEDT.text
            l?.let { it1 -> it1(text.toString()) }
        }

        binding.scannerIV.setOnClickListener {
            barcode()
        }
    }

    private fun barcode() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(activity,  arrayOf(Manifest.permission.CAMERA), 1001)
            return
        }

        val intent = Intent(activity, ScannerActivity::class.java)
        barcodeForResult.launch(intent)

    }

}