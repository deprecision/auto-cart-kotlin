package com.example.smartdrugcart.dialogs

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.view.Window
import android.widget.Toast
import com.example.smartdrugcart.SettingMacAddressActivity
import com.example.smartdrugcart.databinding.DialogDisconnectBinding
import com.example.smartdrugcart.devices.BwDevice

class DisconnectDialog(private var activity: Activity, private var device: BwDevice): Dialog(activity) {

    private var l: ((text: String)->Unit)? = null
    fun setEvent(l: (text: String)->Unit){
        this.l = l
    }

    private val binding: DialogDisconnectBinding by lazy {
        DialogDisconnectBinding.inflate(layoutInflater)
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
            device.reconnect()
        }

    }

    private fun showPasswordDialog(){
        val dialog = PasswordDialog(activity)
        dialog.setEvent {
            if(it == "1111"){
                var intent = Intent(activity, SettingMacAddressActivity::class.java)
                activity.startActivity(intent)
            }else{
                Toast.makeText(activity, "invalid password.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

}