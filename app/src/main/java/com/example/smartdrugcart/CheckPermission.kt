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
import com.bumptech.glide.Glide
import com.example.healthmessage.database.FunctionsLocker
import com.example.smartdrugcart.databinding.ActivityCheckPermissionBinding
import com.example.smartdrugcart.helpers.Prefs
import com.example.smartdrugcart.models.ModelLocker
import java.util.*


class CheckPermission : AppCompatActivity() {
    private val TAG = "CheckPermissionTag"

    private val REQUIRE_CODE_PERMISSION_BLUETOOTH = 1001
    private val binding: ActivityCheckPermissionBinding by lazy {
        ActivityCheckPermissionBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        prefs = Prefs(this)

        //Glide.with(this).asGif().load(R.drawable.ic_app_gif).into(binding.iconAppIV)

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

            timerCount()
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
        Log.i(TAG, "timerCount")
        T = Timer()
        T!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (count == 1) {

                        initData()
                        if(!verifyMacAddress()){
                            prefs.strMacAddress = "DD:65:0C:D3:9A:02"
                            val intent = Intent(this@CheckPermission, SettingMacAddressActivity::class.java)
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

    private fun initData(){
        Log.i(TAG, "initData status: ${prefs.strInitLocker}")
        val functions = FunctionsLocker(this)
        if(prefs.strInitLocker == null){
            functions.insert(ModelLocker(null, null, KEY_ENABLE,"1", "1", 0, "0200310336"))
            functions.insert(ModelLocker(null, null, KEY_ENABLE,"2", "1", 0,"0201310337"))
            functions.insert(ModelLocker(null, null, KEY_ENABLE,"3", "1", 0,"0202310338"))
            functions.insert(ModelLocker(null, null, KEY_ENABLE,"4", "1", 0,"0203310339"))
            functions.insert(ModelLocker(null, null, KEY_ENABLE,"5", "1", 0,"020431033A"))

            functions.insert(ModelLocker(null, null, KEY_PAUSE,"1", "2", 0,""))
            functions.insert(ModelLocker(null, null, KEY_PAUSE,"2", "2", 0,""))
            functions.insert(ModelLocker(null, null, KEY_PAUSE,"3", "2", 0,""))
            functions.insert(ModelLocker(null, null, KEY_PAUSE,"4", "2", 0,""))
            functions.insert(ModelLocker(null, null, KEY_PAUSE,"5", "2", 0,""))

            prefs.strInitLocker = "init"
        }
    }

}