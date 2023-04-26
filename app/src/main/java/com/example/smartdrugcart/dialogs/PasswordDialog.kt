package com.example.smartdrugcart.dialogs

import android.Manifest
import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smartdrugcart.ScannerActivity
import com.example.smartdrugcart.databinding.DialogInputBinding
import com.example.smartdrugcart.databinding.DialogPasswordBinding

class PasswordDialog(private var activity: Activity): Dialog(activity) {

    private var l: ((text: String)->Unit)? = null
    fun setEvent(l: (text: String)->Unit){
        this.l = l
    }

    private val binding: DialogPasswordBinding by lazy {
        DialogPasswordBinding.inflate(layoutInflater)
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(true)

        binding.inputEDT.requestFocus()

        binding.okTV.setOnClickListener {
            val text = binding.inputEDT.text
            l?.let { it1 -> it1(text.toString()) }
        }
    }
}