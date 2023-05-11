package com.example.smartdrugcart.dialogs

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.view.Window
import com.example.smartdrugcart.databinding.DialogUnlockBinding
import com.example.smartdrugcart.models.ModelLocker

class UnlockDialog(private var activity: Activity): Dialog(activity) {

    private var viewType: String? = null

    private val binding: DialogUnlockBinding by lazy {
        DialogUnlockBinding.inflate(layoutInflater)
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
        binding.hnTV.text = "HN: ${model.hn}"
        binding.nameTV.text = "Name: is blank"
        binding.sexTV.text = "Sex: is blank"
        binding.ageTV.text = "Age: is blank"

    }

    fun setTitle(title: String){
        binding.titleTV.text = title
    }

    fun setDescription(description: String){
        binding.subtitleTV.text = description
    }

}