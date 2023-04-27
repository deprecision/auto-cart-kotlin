package com.example.smartdrugcart.dialogs

import android.Manifest
import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.healthmessage.database.FunctionsLocker
import com.example.smartdrugcart.*
import com.example.smartdrugcart.adapters.AdapterLocker
import com.example.smartdrugcart.databinding.DialogRegisterBinding
import com.example.smartdrugcart.devices.DrugCartDevice
import com.example.smartdrugcart.helpers.Prefs
import com.example.smartdrugcart.models.ModelLocker

class RegisterDialog(private var activity: Activity, private var barcodeForResult: ActivityResultLauncher<Intent>): Dialog(activity) {

    private lateinit var prefs: Prefs
    private lateinit var functions: FunctionsLocker
    //dialogs
    private lateinit var dialog: InputHNDialog
    private lateinit var alarmUnlockDialog: AlarmUnlockDialog
    private lateinit var alarmDisconnectDialog: AlarmDisconnectDialog
    private lateinit var device: DrugCartDevice
    //local
    private val drawer1List = ArrayList<ModelLocker>()
    private val drawer2List = ArrayList<ModelLocker>()

//    private var l: (()->Unit)? = null
//    fun setEvent(l: ()->Unit){
//        this.l = l
//    }

    private val binding: DialogRegisterBinding by lazy {
        DialogRegisterBinding.inflate(layoutInflater)
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(false) //false

        init()
        addDataList()
        adapter()
        event()

    }

    private fun event(){
        binding.saveLL.setOnClickListener {
            dismiss()
        }

        binding.closeIV.setOnClickListener {
            dismiss()
        }
    }

//    private fun barcode() {
//        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
//            ActivityCompat.requestPermissions(activity,  arrayOf(Manifest.permission.CAMERA), 1001)
//            return
//        }
//
//        val intent = Intent(activity, ScannerActivity::class.java)
//        barcodeForResult.launch(intent)
//
//    }

    private fun init(){

        prefs = Prefs(activity)
        functions = FunctionsLocker(activity)
        device = DrugCartDevice(activity)
        device.setMyEvent{ event, lockerId->
            when(event){
                DrugCartDevice.STATE_CONNECTED->{
                    alarmDisconnectDialog.dismiss()
                }
                DrugCartDevice.STATE_DISCONNECTED->{
                    alarmDisconnectDialog.show()
                    device.connect()
                }
                DrugCartDevice.STATE_UNLOCK_LOGGER->{
                    showLockerUnlockDialog(lockerId!!)
                    Toast.makeText(activity, "Locker is unlock.", Toast.LENGTH_SHORT).show()
                }
                DrugCartDevice.STATE_LOCK_LOGGER->{
                    hideLockerUnlockDialog()
                    Toast.makeText(activity, "Locker is lock.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        alarmUnlockDialog = AlarmUnlockDialog(activity)
        alarmUnlockDialog!!.setViewType(AlarmUnlockDialog.VIEW_TYPE_REGISTER)

        alarmDisconnectDialog = AlarmDisconnectDialog(activity, device)

        device.connect()
    }

    private fun addDataList(){
        drawer1List.addAll(functions.getDataList())

        drawer2List.add(ModelLocker(6, null, KEY_PAUSE, 0))
        drawer2List.add(ModelLocker(7, null, KEY_PAUSE, 0))
        drawer2List.add(ModelLocker(8, null, KEY_PAUSE, 0))
        drawer2List.add(ModelLocker(9, null, KEY_PAUSE, 0))
        drawer2List.add(ModelLocker(10, null, KEY_PAUSE, 0))
    }

    private fun adapter(){
        val adapter = AdapterLocker(activity, this.drawer1List, KEY_MODE_REGISTER)
        val layoutManager = GridLayoutManager(activity, 5, GridLayoutManager.VERTICAL, false)
        adapter.setMyEvent { event, position->
            when(event){
                AdapterLocker.EVENT_SHOW_INPUTDIALOG->{
                    showInputDialog(position)
                }
                AdapterLocker.EVENT_SHOW_CLEARDIALOG->{
                    showClearDialog(position)
                }
            }
        }
        binding.drawer1RCV.adapter = adapter
        binding.drawer1RCV.layoutManager = layoutManager

        val adapter2 = AdapterLocker(activity, drawer2List, "")
        val layoutManager2 = GridLayoutManager(activity, 5, GridLayoutManager.VERTICAL, false)
        binding.drawer2RCV.adapter = adapter2
        binding.drawer2RCV.layoutManager = layoutManager2

    }

    private fun showInputDialog(position: Int) {

        val dialog = InputHNDialog(activity, barcodeForResult)
        dialog.setUseScanner(false)
        dialog.setEvent { hn ->
            if (hn.isBlank()) {
                Toast.makeText(activity, "ระบุหมายเลข HN", Toast.LENGTH_SHORT).show()
            } else {
                //OPEN DRAWER
                if(this.drawer1List.any { it.hn == hn }){
                    Toast.makeText(activity, "This HN number already exists.", Toast.LENGTH_SHORT).show()
                    dialog.setShowErrorInput(true)
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

                alarmUnlockDialog.setTitle("Locker is unlock")
                alarmUnlockDialog.setSubtitle("Put medicine\nand close it to continue.")

                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showClearDialog(position: Int) {
        var dialog = ClearLockerDialog(activity)
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

                alarmUnlockDialog.setNumber("No.${drawer1List[position].id}")
                alarmUnlockDialog.setSubtitle("Take the pills out of the logger\nand close it to continue.")

            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showAlarmSuccessDialog(){
        val dialog = AlarmSuccessDialog(activity)
        dialog.show()
    }

    private fun showLockerUnlockDialog(lockerId: String){
        val model = drawer1List.single { it.id == lockerId.toLong() }
        alarmUnlockDialog.setNumber("No.${model.id!!}")
        alarmUnlockDialog.show()
    }
    private fun hideLockerUnlockDialog(){
        alarmUnlockDialog.dismiss()
    }

}