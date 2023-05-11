package com.example.smartdrugcart

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.smartdrugcart.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {


    private val binding: ActivitySettingBinding by lazy {
        ActivitySettingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        event()
    }

    private fun event(){
        binding.settingMacAddressLL.setOnClickListener {
            val intent = Intent(this, SettingMacAddressActivity::class.java)
            startActivity(intent)
        }

        binding.backIV.setOnClickListener {
            finish()
        }
    }
}