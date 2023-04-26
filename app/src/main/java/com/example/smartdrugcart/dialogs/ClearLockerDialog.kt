package com.example.smartdrugcart.dialogs

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.view.Window
import com.example.smartdrugcart.databinding.DialogClearLockerBinding

class ClearLockerDialog(private var activity: Activity): Dialog(activity) {

    private var l: ((text: String)->Unit)? = null
    val EVENT_OK = "ok"
    val EVENT_CANCEL = "cancel"

    fun setEvent(l: (event: String)->Unit){
        this.l = l
    }

    private val binding: DialogClearLockerBinding by lazy {
        DialogClearLockerBinding.inflate(layoutInflater)
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(false)


        binding.okTV.setOnClickListener {
            l?.let { it(EVENT_OK) }
        }

        binding.cancelTV.setOnClickListener {
            l?.let { it(EVENT_CANCEL) }
        }
    }

}