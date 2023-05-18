package com.example.smartdrugcart.dialogs

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.view.Window
import com.example.smartdrugcart.databinding.DialogUnlockBinding
import com.example.smartdrugcart.models.ModelLocker

class UnlockDialog(private var activity: Activity): Dialog(activity) {

    companion object{
        val EVENT_SKIP = "eventSkip"
        val EVENT_SUCCESS = "eventSuccess"
    }

    private val binding: DialogUnlockBinding by lazy {
        DialogUnlockBinding.inflate(layoutInflater)
    }

    private var modelLocker: ModelLocker? = null
    private var l: ((event: String) -> Unit?)? = null
    fun setMyEvent(l: ((event: String) -> Unit?)){
        this.l = l
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(false)

        binding.cautionIV.setOnClickListener {
            showCautionDialog()
        }

        setOnDismissListener {
            if(isSkip){
                l?.let { it(EVENT_SKIP) }
            }else{
                l?.let { it(EVENT_SUCCESS) }
            }
            isSkip = false
        }
    }

    fun setModel(model: ModelLocker){
        //set detail
        this.modelLocker = model

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

    private var isSkip = false
    private fun showCautionDialog(){
        val dialog = SkipDialog(activity)
        dialog.setModel(modelLocker!!)
        dialog.setEvent { event ->
            when(event){
                SkipDialog.EVENT_SKIP->{
                    isSkip = true
                    dismiss()
                }
            }
        }
        dialog.show()
    }

}