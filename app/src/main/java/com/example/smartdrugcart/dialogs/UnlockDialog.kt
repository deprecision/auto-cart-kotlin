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
    }

    private val binding: DialogUnlockBinding by lazy {
        DialogUnlockBinding.inflate(layoutInflater)
    }

    private var number: String? = null
    private var l: ((event: String) -> Unit?)? = null
    fun setMyDismiss(l: ((event: String) -> Unit?)){
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

        binding.checkCV.setOnClickListener {
            l?.let { it("checkstatus") }
        }

        setOnDismissListener {
            dialog?.dismiss()
        }
    }

    fun setNumber(number: String){
        //set detail
        this.number = number
        binding.numberTV.text = "No. ${number}"
    }

    fun setTitle(title: String){
        binding.titleTV.text = title
    }

    fun setDescription(description: String){
        binding.subtitleTV.text = description
    }

    private var dialog: SkipDialog? = null
    private fun showCautionDialog(){
        dialog = SkipDialog(activity)
        dialog!!.setNumber(number!!)
        dialog!!.setEvent { event ->
            when(event){
                SkipDialog.EVENT_SKIP->{
                    l?.let { it(EVENT_SKIP) }
                    dismiss()
                }
            }
        }
        dialog!!.show()
    }

}