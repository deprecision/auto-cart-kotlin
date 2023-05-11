package com.example.smartdrugcart.dialogs

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.view.Window
import com.example.smartdrugcart.databinding.DialogOpeningBinding
import com.example.smartdrugcart.models.ModelLocker

class OpeningDialog(private var activity: Activity): Dialog(activity) {

    private val binding: DialogOpeningBinding by lazy {
        DialogOpeningBinding.inflate(layoutInflater)
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(false)
        //have 2 view in 1 dialog
        //have set model
    }

    fun setModel(model: ModelLocker){
        //set detail
        binding.numberTV.text = "No. ${model.position}"
    }

}