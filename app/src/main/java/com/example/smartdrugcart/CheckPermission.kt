package com.example.smartdrugcart

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.smartdrugcart.databinding.ActivityCheckPermissionBinding
import com.example.smartdrugcart.helpers.Prefs
import java.util.*


class CheckPermission : AppCompatActivity() {


    private val REQUIRE_CODE_PERMISSION_BLUETOOTH = 1001
    private val binding: ActivityCheckPermissionBinding by lazy {
        ActivityCheckPermissionBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        prefs = Prefs(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            var permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.i("dwqfqf", "This is Not PERMISSION_GRANTED")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN), REQUIRE_CODE_PERMISSION_BLUETOOTH)
            }else{
                timerCount()
            }
        } else {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if(bluetoothAdapter == null){
                Toast.makeText(this, "อุปกรณ์ไม่รองรับ Bluetooth", Toast.LENGTH_SHORT).show()
                return
            }

            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 1)
            } else {
                timerCount()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i("dwqfqf", "This is onRequestPermissionsResult")
        when(requestCode){
            REQUIRE_CODE_PERMISSION_BLUETOOTH->{

                val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    timerCount()
                }else{
                    finish()
                }
            }
        }

    }

    var T: Timer? = null
    var count = 0
    private fun timerCount() {
        T = Timer()
        T!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (count == 1) {

                        if(!verifyMacAddress()){
                            prefs.strMacAddress = "DD:65:0C:D3:9A:02"
                            val intent = Intent(this@CheckPermission, SettingActivity::class.java)
                            startActivity(intent)
                            finish()
                        }else {
                            val intent = Intent(this@CheckPermission, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                    count++
                }
            }
        }, 0, 1800)
    }

    private lateinit var prefs: Prefs
    private fun verifyMacAddress(): Boolean{
        return prefs.strMacAddress != null
    }

}