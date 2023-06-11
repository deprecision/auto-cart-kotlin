package com.example.smartdrugcart

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.healthmessage.database.FunctionsLocker
import com.example.smartdrugcart.adapters.AdapterLocker
import com.example.smartdrugcart.databinding.ActivityRegisterBinding
import com.example.smartdrugcart.devices.BwDevice
import com.example.smartdrugcart.dialogs.*
import com.example.smartdrugcart.models.ModelLocker
import java.util.*


class RegisterActivity : AppCompatActivity() {
    private val TAG = "RegisterActivityTag"

    private lateinit var functions: FunctionsLocker
    //dialogs
    private lateinit var bwDevice: BwDevice
    //local
    private val drawer1List = ArrayList<ModelLocker>()
    private val drawer2List = ArrayList<ModelLocker>()

    private var lastInputHN: String? = ""
    private var lastPosition = -1
    private var lastDrawerAt = ""

    private val binding: ActivityRegisterBinding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }

    override fun onBackPressed() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setFinishOnTouchOutside(false)

        init()
        addDataList()
        adapter()

        event()

    }

    private fun init(){

        initUnlockDialog()
        openingDialog = OpeningDialog(this)

        functions = FunctionsLocker(this)
        bwDevice = BwDevice(this)
        bwDevice.setMyEvent{ event->
            when(event){
                BwDevice.STATE_CONNECTED->{
                    hideDisconnectDialog()
                }
                BwDevice.STATE_DISCONNECTED->{
                    showDisconnectDialog()
                    bwDevice.reconnect()
                }
                BwDevice.STATE_UNLOCK_LOCKER->{
                    if(lastPosition == -1) return@setMyEvent
                    i = 0
                    hideOpeningDialog()

                    if(unlockDialog.isShowing){
                        unlockDialog.setTitle("Locker is unlock")

                        if(countUnlock != 0){
                            countUnlock = 0
                            Toast.makeText(this, "Please close the drawer completely.", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        showUnlockDialog(lastPosition)
                        Toast.makeText(this, "Locker is unlock.", Toast.LENGTH_SHORT).show()
                    }


                }
                BwDevice.STATE_LOCK_LOCKER->{
                    if(lastPosition == -1) return@setMyEvent

                    if(openingDialog.isShowing){

                    }else{

                        countUnlock++
                        unlockDialog!!.setTitle("Checking drawer status...")
                        if(countUnlock == 3){
                            countUnlock = 0
                            hideUnlockDialog()
                            updateData()
                            Toast.makeText(this, "Locker is lock.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        bwDevice.connect()

    }

    private fun event(){

        binding.saveLL.setOnClickListener {
            finish()
        }

    }

    private fun addDataList(){
        drawer1List.addAll(functions.getDataListDrawerAt("1"))
        drawer2List.addAll(functions.getDataListDrawerAt("2"))
    }

    private fun adapter(){
        val adapter = AdapterLocker(this, drawer1List, KEY_MODE_REGISTER)
        val layoutManager = GridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false)
        adapter.setMyEvent { event, position->
            when(event){
                AdapterLocker.EVENT_SHOW_INPUTDIALOG->{

                    lastDrawerAt = "1"
                    lastPosition = position

                    showInputDialog(position)
                }
            }
        }
        binding.drawer1RCV.adapter = adapter
        binding.drawer1RCV.layoutManager = layoutManager

        val adapter2 = AdapterLocker(this, drawer2List, "")
        val layoutManager2 = GridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false)
        binding.drawer2RCV.adapter = adapter2
        binding.drawer2RCV.layoutManager = layoutManager2

    }


    private var inputHNDialog: InputHNDialog? = null
    private fun showInputDialog(position: Int) {
        inputHNDialog = InputHNDialog(this)
        inputHNDialog!!.setEvent { hn ->

            if (hn.isBlank()) {
                Toast.makeText(this, "Please enter HN number", Toast.LENGTH_SHORT).show()
                inputHNDialog!!.setShowErrorInput(true, "*Please enter HN number.")
                return@setEvent
            }
            if (drawer1List.any { it.hn == hn }) {
                Toast.makeText(this, "This HN number already exists.", Toast.LENGTH_SHORT).show()
                inputHNDialog!!.setShowErrorInput(true, "*This HN number already exists.")
                return@setEvent
            }

            lastInputHN = hn
            showOpeningDialog(position)

            inputHNDialog!!.dismiss()
        }
        inputHNDialog!!.show()
    }


    private var disconnectDialog: DisconnectDialog? = null
    private fun showDisconnectDialog(){
        if(disconnectDialog == null){
            disconnectDialog = DisconnectDialog(this, bwDevice)
        }
        disconnectDialog!!.show()
    }
    private fun hideDisconnectDialog(){
        disconnectDialog?.dismiss()
    }

    private var countUnlock = 0
    private lateinit var unlockDialog: UnlockDialog
    private fun initUnlockDialog(){
        unlockDialog = UnlockDialog(this)
        unlockDialog.setTitle("Locker is unlock")
        unlockDialog.setDescription("Put the pills in the drawer.")
        unlockDialog.setMyDismiss { event ->
            when(event){
                UnlockDialog.EVENT_SKIP->{
                    lastPosition = -1
                    countUnlock = 0
                }
                else->{
                    bwDevice.checkStatusLockerAll()
                    Toast.makeText(this, "Check status locker.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun showUnlockDialog(position: Int){
        var number = position + 1
        unlockDialog.setTitle("Locker is unlock")
        unlockDialog.setNumber(number.toString())
        unlockDialog.show()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable{
            override fun run() {

                if(unlockDialog.isShowing){
                    bwDevice.checkStatusLockerAll()
                    handler.postDelayed(this, 1000)
                }
            }
        }, 1000)

    }
    private fun hideUnlockDialog(){
        unlockDialog.dismiss()
    }

    private var i = 0
    private lateinit var openingDialog: OpeningDialog
    private fun showOpeningDialog(position: Int){
        openingDialog.setModel(drawer1List[position])
        openingDialog.show()

        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable{
            override fun run() {

                if(openingDialog.isShowing){

                    if(i == 3){
                        i = 0
                        hideOpeningDialog()
                        Toast.makeText(this@RegisterActivity, "try again.", Toast.LENGTH_SHORT).show()
                        return
                    }
                    i++

                    unlockLocker(position)

                    Thread.sleep(1000)
                    bwDevice.checkStatusLockerAll()
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }
    private fun hideOpeningDialog(){
        openingDialog.dismiss()
    }

    private fun updateData(){
        //update data
        drawer1List[lastPosition].hn = lastInputHN
        drawer1List[lastPosition].counter = 0
        functions.update(drawer1List[lastPosition])

        binding.drawer1RCV.adapter?.notifyItemChanged(lastPosition)
    }

    private fun unlockLocker(position: Int){
        val locker = drawer1List[position]
        bwDevice.sendCommand(locker.position!!.toInt(), locker.drawerAt!!.toInt(), locker.cmdUnlock!!)
    }

}