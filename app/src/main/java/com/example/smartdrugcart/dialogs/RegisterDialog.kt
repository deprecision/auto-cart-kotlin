package com.example.smartdrugcart.dialogs

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.GridLayoutManager
import com.example.healthmessage.database.FunctionsLocker
import com.example.smartdrugcart.*
import com.example.smartdrugcart.adapters.AdapterLocker
import com.example.smartdrugcart.databinding.DialogRegisterBinding
import com.example.smartdrugcart.devices.BwDevice
import com.example.smartdrugcart.models.ModelLocker
import kotlin.collections.ArrayList

class RegisterDialog(private var activity: Activity, private var barcodeForResult: ActivityResultLauncher<Intent>): Dialog(activity) {


    private lateinit var functions: FunctionsLocker
    //dialogs
    private lateinit var device: BwDevice
    //local
    private val drawer1List = ArrayList<ModelLocker>()
    private val drawer2List = ArrayList<ModelLocker>()

    private var lastInputHN: String? = ""
    private var lastPosition = 0

//    private var l: (()->Unit)? = null
//    fun setEvent(l: ()->Unit){
//        this.l = l
//    }

    fun setInputHn(hn: String){

        if(drawer1List.any { it.hn == hn }){
            Toast.makeText(activity, "This HN number already exists.", Toast.LENGTH_SHORT).show()
            inputHNDialog!!.setShowErrorInput(true)
            return
        }
        lastInputHN = hn
        unlockLocker()
        unlockDialog!!.setDescription("Put medicine\nand close it to continue.")//set message is put medicine

        inputHNDialog!!.dismiss()

        Log.i("fewfw", "setInputHn: ${hn}" )
    }

    private val binding: DialogRegisterBinding by lazy {
        DialogRegisterBinding.inflate(layoutInflater)
    }

    private var isConnect = false
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        setCancelable(false) //false
        show()

        init()
        addDataList()
        adapter()
        event()

        Log.i("dawda", "init")
    }

    private fun event(){
        binding.saveLL.setOnClickListener {
            dismiss()
        }

        binding.closeIV.setOnClickListener {
            dismiss()
        }
    }

    private fun init(){

        initUnlockDialog()
        functions = FunctionsLocker(activity)
        device = BwDevice(activity)
        device.setMyEvent{ event->
            when(event){
                BwDevice.STATE_CONNECTED->{
                    isConnect = true
                    hideDisconnectDialog()
                }
                BwDevice.STATE_DISCONNECTED->{
                    showDisconnectDialog()
                    device.connect()
                }
                BwDevice.STATE_UNLOCK_LOGGER->{
                    val lockerId = drawer1List[lastPosition].id!!
                    showUnlockDialog(lockerId.toString())
                    Log.i("ewfwef", "Register STATE_UNLOCK_LOGGER")
                }
                BwDevice.STATE_LOCK_LOGGER->{
                    hideUnlockDialog()
                    Log.i("ewfwef", "Register STATE_LOCK_LOGGER")
                }
            }
        }
        device.connect()

        Handler().postDelayed({
            if(!isConnect){
                showDisconnectDialog()
            }
        }, 1000)

    }

    private fun addDataList(){
        drawer1List.addAll(functions.getDataListDrawerAt("1"))
        drawer2List.addAll(functions.getDataListDrawerAt("2"))
    }

    private fun adapter(){
        val adapter = AdapterLocker(activity, drawer1List, KEY_MODE_REGISTER)
        val layoutManager = GridLayoutManager(activity, 5, GridLayoutManager.VERTICAL, false)
        adapter.setMyEvent { event, position->
            when(event){
                AdapterLocker.EVENT_SHOW_INPUTDIALOG->{
                    showInputDialog(position)
                }
                AdapterLocker.EVENT_SHOW_ENABLEDIALOG->{
                    //showClearDialog(position)
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

    private var inputHNDialog: InputHNDialog? = null
    private fun showInputDialog(position: Int) {
        lastPosition = position
        inputHNDialog = InputHNDialog(activity, barcodeForResult)
        inputHNDialog!!.setEvent { hn ->
            if (hn.isBlank()) {
                Toast.makeText(activity, "ระบุหมายเลข HN", Toast.LENGTH_SHORT).show()
            } else {
                //OPEN DRAWER
                if(drawer1List.any { it.hn == hn }){
                    Toast.makeText(activity, "This HN number already exists.", Toast.LENGTH_SHORT).show()
                    inputHNDialog!!.setShowErrorInput(true)
                    return@setEvent
                }
                lastInputHN = hn

                unlockLocker()
                unlockDialog!!.setDescription("Put medicine\nand close it to continue.")//set message is put medicine
                inputHNDialog!!.dismiss()

                val lockerId = drawer1List[lastPosition].id!!
                showUnlockDialog(lockerId.toString())
            }
        }
        inputHNDialog!!.show()
    }

    private fun showClearDialog(position: Int) {
        lastPosition = position
        val dialog = ClearLockerDialog(activity)
        dialog.setEvent { event ->
            if (event == dialog.EVENT_OK) {

                lastInputHN = null
                //unlock logger
                unlockLocker()
                unlockDialog!!.setDescription("Take the pills out of the locker\nand close it to continue.")//set message is clear medicine
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun unlockLocker(){
        val lockerId = drawer1List[lastPosition].id!!.toInt()
        //device.sendCmd(lockerId)
    }



    private var unlockDialog: UnlockDialog? = null
    private fun initUnlockDialog(){
        unlockDialog = UnlockDialog(activity)
        unlockDialog!!.setOnDismissListener {
            updateData()
            showSuccessDialog()
            Toast.makeText(activity, "Locker is lock.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showUnlockDialog(lockerId: String){

        Log.i("adwdad", "Register showUnlockDialog")
        if(unlockDialog!!.isShowing) return

        val model = functions.getDataById(lockerId)
        if(model != null){
            unlockDialog!!.setTitle("Locker is unlock")
            unlockDialog!!.show()
            Toast.makeText(activity, "Locker is unlock.", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(activity, "Not found in data", Toast.LENGTH_SHORT).show()
        }
    }
    private fun hideUnlockDialog(){
        unlockDialog?.dismiss()
    }


    private var disconnectDialog: DisconnectDialog? = null
    private fun showDisconnectDialog(){
        if(disconnectDialog == null){
            disconnectDialog = DisconnectDialog(activity, device)
        }
        disconnectDialog!!.show()
    }
    private fun hideDisconnectDialog(){
        disconnectDialog?.dismiss()
    }


    private fun showSuccessDialog(){
        val dialog = SuccessDialog(activity)
        dialog.show()
    }

    private fun updateData(){
        //update data
        drawer1List[lastPosition].hn = lastInputHN
        drawer1List[lastPosition].counter = 0
        functions.update(drawer1List[lastPosition])

        binding.drawer1RCV.adapter?.notifyItemChanged(lastPosition)

    }

}