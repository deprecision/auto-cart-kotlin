package com.example.smartdrugcart.dialogs

import android.Manifest
import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.view.Window
import android.widget.Toast
import com.example.smartdrugcart.SettingActivity
import com.example.smartdrugcart.databinding.DialogAlarmDisconnectBinding
import com.example.smartdrugcart.devices.DrugCartDevice

class AlarmDisconnectDialog(private var activity: Activity, private var device: DrugCartDevice): Dialog(activity) {

    private var l: ((text: String)->Unit)? = null
    fun setEvent(l: (text: String)->Unit){
        this.l = l
    }

    private val binding: DialogAlarmDisconnectBinding by lazy {
        DialogAlarmDisconnectBinding.inflate(layoutInflater)
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(false) //false

        binding.settingLL.setOnClickListener {
            showPasswordDialog()
        }

        binding.refreshLL.setOnClickListener {
            Toast.makeText(activity, "reconnected.", Toast.LENGTH_SHORT).show()
            device.connect()
        }

    }

    private fun showPasswordDialog(){
        val dialog = PasswordDialog(activity)
        dialog.setEvent {
            if(it == "1111"){
                var intent = Intent(activity, SettingActivity::class.java)
                activity.startActivity(intent)
            }else{
                Toast.makeText(activity, "invalid password.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

}