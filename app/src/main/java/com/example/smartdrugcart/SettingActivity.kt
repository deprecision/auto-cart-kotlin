package com.example.smartdrugcart

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.smartdrugcart.databinding.ActivityMainBinding
import com.example.smartdrugcart.databinding.ActivitySettingBinding
import com.example.smartdrugcart.helpers.Prefs

class SettingActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private val binding: ActivitySettingBinding by lazy {
        ActivitySettingBinding.inflate(layoutInflater)
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

        binding.scannerIV.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            barcodeForResult.launch(intent)
        }
    }

    private val barcodeForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            Log.i("daswg3gs", "This is registerForActivityResult.")

            val data: Intent? = result.data

            if(data != null){
                val barcode = data?.getStringExtra("SCAN_RESULT")
                binding.inputEDT.setText(barcode)

                Toast.makeText(this, barcode, Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, "fail", Toast.LENGTH_LONG).show()
            }
        }
    }

}