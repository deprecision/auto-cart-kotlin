package com.example.smartdrugcart

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.smartdrugcart.databinding.ActivitySettingMacAddressBinding
import com.example.smartdrugcart.helpers.Prefs

class SettingMacAddressActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private val binding: ActivitySettingMacAddressBinding by lazy {
        ActivitySettingMacAddressBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        prefs = Prefs(this)
        binding.inputEDT.setText(prefs.strMacAddress)
        event()
    }

    private fun event(){

        binding.okTV.setOnClickListener {
            var value = binding.inputEDT.text.toString()

            if(value.isBlank()){
                Toast.makeText(this, "Pleace Input MAC Address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(!isValidMacAddress(value)){
                Toast.makeText(this, "Format Error.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var prefs = Prefs(this)
            prefs.strMacAddress = value
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()

        }

        binding.backIV.setOnClickListener {
            finish()
        }
    }

}