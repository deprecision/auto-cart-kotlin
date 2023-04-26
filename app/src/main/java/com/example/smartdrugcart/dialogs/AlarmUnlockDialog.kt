package com.example.smartdrugcart.dialogs

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.view.View
import android.view.Window
import com.example.smartdrugcart.databinding.DialogAlarmUnlockBinding
import com.example.smartdrugcart.models.ModelLocker

class AlarmUnlockDialog(private var activity: Activity): Dialog(activity) {

    companion object{
        val VIEW_TYPE_REGISTER = "register"
        val VIEW_TYPE_PAY = "pay"
    }

    private var viewType: String? = null

    private val binding: DialogAlarmUnlockBinding by lazy {
        DialogAlarmUnlockBinding.inflate(layoutInflater)
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

    fun setViewType(viewType: String){
        this.viewType = viewType
        when(viewType){
            VIEW_TYPE_REGISTER->{

            }
            VIEW_TYPE_PAY->{

            }
        }
    }

    fun setModelDetail(model: ModelLocker?){
        //set detail

    }

    fun setNumber(number: String){
        binding.numberTV.text = number
    }

    fun setTitle(title: String){
        binding.titleTV.text = title
    }

    fun setSubtitle(subtitle: String){
        binding.subtitleTV.text = subtitle
    }

}