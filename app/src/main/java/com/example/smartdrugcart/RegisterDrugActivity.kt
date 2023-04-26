package com.example.smartdrugcart

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.healthmessage.database.FunctionsLocker
import com.example.smartdrugcart.adapters.AdapterLogger
import com.example.smartdrugcart.databinding.ActivityRegisterDrugBinding
import com.example.smartdrugcart.devices.DrugCartDevice
import com.example.smartdrugcart.dialogs.*
import com.example.smartdrugcart.helpers.Prefs
import com.example.smartdrugcart.models.ModelLocker


class RegisterDrugActivity : AppCompatActivity() {
    private val TAG = "RegisterDrugActivityTag"

    private lateinit var prefs: Prefs
    private lateinit var functions: FunctionsLocker
    //dialogs
    private lateinit var dialog: InputDialog
    private lateinit var alarmUnlockDialog: AlarmUnlockDialog
    private lateinit var alarmDisconnectDialog: AlarmDisconnectDialog
    private lateinit var device: DrugCartDevice
    //local
    private val drawer1List = ArrayList<ModelLocker>()
    private val drawer2List = ArrayList<ModelLocker>()

    private val binding: ActivityRegisterDrugBinding by lazy {
        ActivityRegisterDrugBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
        addDataList()
        adapter()
        event()
    }

    override fun onDestroy() {
        super.onDestroy()
        device.destroy()
    }

    private fun init(){

        prefs = Prefs(this)
        functions = FunctionsLocker(this)
        alarmUnlockDialog = AlarmUnlockDialog(this)
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
                    showLoggerUnlockDialog()
                    Toast.makeText(this, "Logger is unlock.", Toast.LENGTH_SHORT).show()
                }
                DrugCartDevice.STATE_LOCK_LOGGER->{
                    hideLoggerUnlockDialog()
                    Toast.makeText(this, "Logger is lock.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.macAddressTV.text = prefs.strMacAddress
        binding.stateDeviceTV.text = KEY_DISSCONNET
        binding.stateDeviceTV.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
        binding.stateDeviceIV.setColorFilter(ContextCompat.getColor(this, R.color.colorRed), PorterDuff.Mode.SRC_ATOP)

        device.connect()

        alarmUnlockDialog.setOnDismissListener {
            showAlarmSuccessDialog()
        }
    }

    private fun addDataList(){
        drawer1List.addAll(functions.getDataList())

        drawer2List.add(ModelLocker(null, null, KEY_LOCK, 0))
        drawer2List.add(ModelLocker(null, null, KEY_LOCK, 0))
        drawer2List.add(ModelLocker(null, null, KEY_LOCK, 0))
        drawer2List.add(ModelLocker(null, null, KEY_LOCK, 0))
        drawer2List.add(ModelLocker(null, null, KEY_LOCK, 0))
    }

    private fun adapter(){
        val adapter = AdapterLogger(this, this.drawer1List, KEY_MODE_REGISTER)
        val layoutManager = GridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false)
        adapter.setMyEvent { event, position->
            when(event){
                AdapterLogger.EVENT_SHOW_INPUTDIALOG->{
                    showInputDialog(position)
                }
                AdapterLogger.EVENT_SHOW_CLEARDIALOG->{
                    showClearDialog(position)
                }
            }
        }
        binding.drawer1RCV.adapter = adapter
        binding.drawer1RCV.layoutManager = layoutManager

        val adapter2 = AdapterLogger(this, drawer2List, KEY_MODE_PAY)
        val layoutManager2 = GridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false)
        binding.drawer2RCV.adapter = adapter2
        binding.drawer2RCV.layoutManager = layoutManager2

    }

    private fun event(){

        binding.backLL.setOnClickListener {
            finish()
        }
    }

    private val barcodeForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            dialog?.dismiss()

            Log.i("daswg3gs", "This is registerForActivityResult.")

            val data: Intent? = result.data

            if(data != null){
                val barcodeText = data?.getStringExtra("SCAN_RESULT")
                //

                Toast.makeText(this, barcodeText, Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, "ไม่สำเร็จ", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showInputDialog(position: Int) {

        var dialog = InputDialog(this, barcodeForResult)
        dialog.setEvent { hn ->
            if (hn.isBlank()) {
                Toast.makeText(this, "ระบุหมายเลข HN", Toast.LENGTH_SHORT).show()
            } else {
                //OPEN DRAWER
                if(this.drawer1List.any { it.hn == hn }){
                    Toast.makeText(this, "This HN number already exists.", Toast.LENGTH_SHORT).show()
                    return@setEvent
                }

                this.drawer1List[position].hn = hn
                this.drawer1List[position].counter = 0
                //update data
                functions.update(this.drawer1List[position])
                //unlock logger
                device.unlock(this.drawer1List[position].id!!.toInt())
                //change notify
                binding.drawer1RCV.adapter?.notifyItemChanged(position)

                alarmUnlockDialog.setTitle("Logger is unlock")
                alarmUnlockDialog.setSubtitle("Put medicine\nand close it to continue.")
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showClearDialog(position: Int) {
        var dialog = ClearLockerDialog(this)
        dialog.setEvent { event ->
            if (event == dialog.EVENT_OK) {
                this.drawer1List[position].hn = null
                this.drawer1List[position].counter = 0
                //update data
                functions.update(this.drawer1List[position])
                //unlock logger
                device.unlock(this.drawer1List[position].id!!.toInt())
                //change notify
                binding.drawer1RCV.adapter?.notifyItemChanged(position)

                alarmUnlockDialog.setTitle("Logger is unlock")
                alarmUnlockDialog.setSubtitle("Take the pills out of the logger\nand close it to continue.")

            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showAlarmSuccessDialog(){
        val dialog = AlarmSuccessDialog(this)
        dialog.show()
    }

    private fun showLoggerUnlockDialog(){
        alarmUnlockDialog.show()
    }
    private fun hideLoggerUnlockDialog(){
        alarmUnlockDialog.dismiss()
    }

}