package com.example.smartdrugcart

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.healthmessage.database.FunctionsLocker
import com.example.smartdrugcart.adapters.AdapterLogger
import com.example.smartdrugcart.databinding.ActivityMainBinding
import com.example.smartdrugcart.devices.DrugCartDevice
import com.example.smartdrugcart.dialogs.*
import com.example.smartdrugcart.helpers.Prefs
import com.example.smartdrugcart.models.ModelLocker


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivityTag"

    private lateinit var prefs: Prefs
    private lateinit var functions: FunctionsLocker
    //dialogs
    private lateinit var inputDialog: InputDialog
    private lateinit var device: DrugCartDevice
    private lateinit var alarmDisconnectDialog: AlarmDisconnectDialog
    private var alarmUnlockDialog: AlarmUnlockDialog? = null
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val drawer1List = ArrayList<ModelLocker>()
    private val drawer2List = ArrayList<ModelLocker>()
    private var positionCurrent = 0

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //DD:65:0C:D3:9A:02

        init()
        adapter()
        event()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        addDataList()
        binding.drawer1RCV.adapter?.notifyDataSetChanged()

        device.connect()
    }

    override fun onPause() {
        super.onPause()

        device.destroy()
    }

    private fun init(){

        prefs = Prefs(this)
        functions = FunctionsLocker(this)
        alarmDisconnectDialog = AlarmDisconnectDialog(this)
        device = DrugCartDevice(this)
        device.setMyEvent{ event ->
            when(event){
                DrugCartDevice.STATE_CONNECTED->{
                    binding.stateDeviceTV.text = KEY_CONNECT
                    binding.stateDeviceTV.setTextColor(ContextCompat.getColor(this, R.color.colorGreen))
                    binding.stateDeviceIV.setColorFilter(ContextCompat.getColor(this, R.color.colorGreen), PorterDuff.Mode.SRC_ATOP)
                    alarmDisconnectDialog.dismiss()
                }
                DrugCartDevice.STATE_DISCONNECTED->{
                    binding.stateDeviceTV.text = KEY_DISSCONNET
                    binding.stateDeviceTV.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
                    binding.stateDeviceIV.setColorFilter(ContextCompat.getColor(this, R.color.colorRed), PorterDuff.Mode.SRC_ATOP)
                    alarmDisconnectDialog.show()
                }
                DrugCartDevice.STATE_UNLOCK_LOGGER->{
                    showAlarmUnlockDialog()
                    Toast.makeText(this, "Logger is unlock.", Toast.LENGTH_SHORT).show()
                }
                DrugCartDevice.STATE_LOCK_LOGGER->{
                    hideAlarmUnlockDialog()
                    Toast.makeText(this, "Logger is lock.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.macAddressTV.text = prefs.strMacAddress
        binding.stateDeviceTV.text = KEY_DISSCONNET
        binding.stateDeviceTV.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
        binding.stateDeviceIV.setColorFilter(ContextCompat.getColor(this, R.color.colorRed), PorterDuff.Mode.SRC_ATOP)
        alarmDisconnectDialog.show()


    }

    private fun addDataList(){
        drawer1List.clear()
        drawer2List.clear()
        val dataList = functions.getDataList()
        when(dataList.size){
            0->{
                functions.insert(ModelLocker(null, null, KEY_LOCK, 0))
                functions.insert(ModelLocker(null, null, KEY_LOCK, 0))
                functions.insert(ModelLocker(null, null, KEY_LOCK, 0))
                functions.insert(ModelLocker(null, null, KEY_LOCK, 0))
                functions.insert(ModelLocker(null, null, KEY_LOCK, 0))
                drawer1List.addAll(functions.getDataList())

                drawer2List.add(ModelLocker(null, null, KEY_LOCK, 0))
                drawer2List.add(ModelLocker(null, null, KEY_LOCK, 0))
                drawer2List.add(ModelLocker(null, null, KEY_LOCK, 0))
                drawer2List.add(ModelLocker(null, null, KEY_LOCK, 0))
                drawer2List.add(ModelLocker(null, null, KEY_LOCK, 0))
            }
            else->{
                drawer1List.addAll(dataList)

                drawer2List.add(ModelLocker(null, null, KEY_LOCK, 0))
                drawer2List.add(ModelLocker(null, null, KEY_LOCK, 0))
                drawer2List.add(ModelLocker(null, null, KEY_LOCK, 0))
                drawer2List.add(ModelLocker(null, null, KEY_LOCK, 0))
                drawer2List.add(ModelLocker(null, null, KEY_LOCK, 0))
            }
        }
        Log.i(TAG, "loggerList size: " + drawer1List.size)
    }

    private fun adapter(){
        val adapter1 = AdapterLogger(this, drawer1List, KEY_MODE_PAY)
        val layoutManager = GridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false)
        binding.drawer1RCV.adapter = adapter1
        binding.drawer1RCV.layoutManager = layoutManager

        val adapter2 = AdapterLogger(this, drawer2List, KEY_MODE_PAY)
        val layoutManager2 = GridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false)
        binding.drawer2RCV.adapter = adapter2
        binding.drawer2RCV.layoutManager = layoutManager2

    }

    private fun event(){

        binding.settingIV.setOnClickListener {
            showPasswordDialog()
        }

        binding.historyIV.setOnClickListener {

        }

        binding.registerLL.setOnClickListener {
            var intent = Intent(this, RegisterDrugActivity::class.java)
            startActivity(intent)
        }

        binding.payLL.setOnClickListener {
            inputDialog = InputDialog(this, barcodeForResult)
            inputDialog!!.setEvent { hn->
                openLogger(hn)
            }
            inputDialog!!.show()
        }

    }

    private fun showPasswordDialog(){
        val dialog = PasswordDialog(this)
        dialog.setEvent {
            if(it == "1111"){
                var intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(this, "invalid password.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private val barcodeForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            inputDialog?.dismiss()

            Log.i("daswg3gs", "This is registerForActivityResult.")

            val data: Intent? = result.data

            if(data != null){
                val barcode = data?.getStringExtra("SCAN_RESULT")
                openLogger(barcode)

                Toast.makeText(this, barcode, Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, "fail", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openLogger(hn: String?){

        if(hn == null){
            Toast.makeText(this, "ระบุหมายเลข HN", Toast.LENGTH_SHORT).show()
            return
        }

        if(hn.isBlank()){
            Toast.makeText(this, "ระบุหมายเลข HN", Toast.LENGTH_SHORT).show()
            return
        }


        val model = drawer1List.singleOrNull { it.hn == hn }
        if(model != null){
            val position = drawer1List.indexOf(model)
            positionCurrent = position
            device?.unlock(model.id!!.toInt())
            inputDialog!!.dismiss()
        }else{
            Toast.makeText(this, "Not Found", Toast.LENGTH_SHORT).show()
        }

    }

    private fun showClearDialog(position: Int) {
        var dialog = ClearLockerDialog(this)
        dialog.setEvent { event ->
            if (event == dialog.EVENT_OK) {
                drawer1List[position].hn = null
                drawer1List[position].counter = 0
                //update data
                functions.update(drawer1List[position])

            }else{
                //update data
                drawer1List[position].counter = drawer1List[position].counter!! + 1
                functions.update(drawer1List[position])
            }
            //change notify
            binding.drawer1RCV.adapter?.notifyItemChanged(position)
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun showAlarmUnlockDialog(){
        if(alarmUnlockDialog == null){
            alarmUnlockDialog = AlarmUnlockDialog(this)
            alarmUnlockDialog!!.setTitle("Logger is unlock")
            alarmUnlockDialog!!.setSubtitle("Take the pills out of the logger\nand close it to continue.")
            alarmUnlockDialog!!.setCancelable(false)
            alarmUnlockDialog!!.setOnDismissListener {
                showClearDialog(positionCurrent)
            }
        }
        alarmUnlockDialog!!.show()
    }
    private fun hideAlarmUnlockDialog(){
        alarmUnlockDialog?.dismiss()
    }


}