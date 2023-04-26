package com.example.smartdrugcart.dialogs

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.view.Window
import com.example.smartdrugcart.databinding.DialogAlarmUnlockBinding

class AlarmUnlockDialog(private var activity: Activity): Dialog(activity) {

    private val binding: DialogAlarmUnlockBinding by lazy {
        DialogAlarmUnlockBinding.inflate(layoutInflater)
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(false)



    }

    fun setTitle(title: String){
        binding.titleTV.text = title
    }

    fun setSubtitle(subtitle: String){
        binding.subtitleTV.text = subtitle
    }

}