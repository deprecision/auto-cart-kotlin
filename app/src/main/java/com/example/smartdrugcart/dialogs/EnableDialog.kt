package com.example.smartdrugcart.dialogs

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.view.Window
import com.example.smartdrugcart.databinding.DialogEnableBinding
import com.example.smartdrugcart.models.ModelLocker

class EnableDialog(private var activity: Activity): Dialog(activity) {

    companion object{
        val EVENT_ENABLE = "enable"
        val EVENT_CANCEL = "cancel"
    }

    private val binding: DialogEnableBinding by lazy {
        DialogEnableBinding.inflate(layoutInflater)
    }

    private var l: ((event: String) -> Unit)? = null
    fun setEvent(l: ((event: String) -> Unit)){
        this.l = l
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(true)

        binding.cancelTV.setOnClickListener {
            l?.let { it(EVENT_CANCEL) }
            dismiss()
        }

        binding.enableTV.setOnClickListener {
            l?.let { it(EVENT_ENABLE) }
            dismiss()
        }
    }

    fun setModel(model: ModelLocker){
        //set detail
        binding.numberTV.text = "No. ${model.position}"
    }

}