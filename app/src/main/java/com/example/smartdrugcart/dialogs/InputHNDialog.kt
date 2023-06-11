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
import com.example.smartdrugcart.databinding.DialogInputHnBinding

class InputHNDialog(private var activity: Activity): Dialog(activity) {

    private val binding: DialogInputHnBinding by lazy {
        DialogInputHnBinding.inflate(layoutInflater)
    }


    private var l: ((text: String)->Unit)? = null
    fun setEvent(l: (text: String)->Unit){
        this.l = l
    }

    fun setShowErrorInput(isShow: Boolean, message:String){
        when(isShow){
            true->{
                binding.messageTV.visibility = View.VISIBLE
                binding.messageTV.text = message
            }
            false->{
                binding.messageTV.visibility = View.GONE
                binding.messageTV.text = message
            }
        }
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
    }

}