package com.example.smartdrugcart

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.healthmessage.database.FunctionsLocker
import com.example.smartdrugcart.adapters.AdapterLocker
import com.example.smartdrugcart.databinding.ActivityRegisterBinding
import com.example.smartdrugcart.devices.BwDevice
import com.example.smartdrugcart.dialogs.AlarmDisconnectDialog
import com.example.smartdrugcart.dialogs.AlarmSuccessDialog
import com.example.smartdrugcart.dialogs.AlarmUnlockDialog
import com.example.smartdrugcart.dialogs.InputHNDialog
import com.example.smartdrugcart.models.ModelLocker
import java.util.*
import kotlin.concurrent.schedule


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
        initTTS()
        addDataList()
        adapter()

        event()
    }

    private fun init(){

        functions = FunctionsLocker(this)
        bwDevice = BwDevice(this)
        bwDevice.setMyEvent{ event->
            when(event){
                BwDevice.STATE_CONNECTED->{
                    Log.i(TAG, "Register STATE_CONNECTED")
                    hideDisconnectDialog()
                }
                BwDevice.STATE_DISCONNECTED->{
                    Log.i(TAG, "Register STATE_DISCONNECTED")
                    showDisconnectDialog()
                    bwDevice.reconnect()
                }
                BwDevice.STATE_UNLOCK_LOGGER->{
                    showUnlockDialog()
                    if(currentPositionVerify != -1){
                        currentPositionVerify = -1   //reset value
                        speak("Please close the drawer completely.")
                    }
                    Log.i(TAG, "Register STATE_UNLOCK_LOGGER")
                }
                BwDevice.STATE_LOCK_LOGGER->{

                    if(lastPosition == currentPositionVerify){
                        hideUnlockDialog()
                        currentPositionVerify = -1 //reset value
                        Toast.makeText(this, "Locker is lock.", Toast.LENGTH_SHORT).show()
                        Log.i(TAG, "Register STATE_LOCK_LOGGER")
                    }else{
                        currentPositionVerify = lastPosition //set for check
                        unlockDialog!!.setTitle("Checking drawer status...")
                        Timer().schedule(3000) {
                            Log.i(TAG, "Timer.")
                            bwDevice.checkStatusLockerAll()
                        }
                        Log.i(TAG, "Register STATE_LOCK_LOGGER Checking...")
                    }
                }
            }
        }
        bwDevice.connect()

    }

    private var textToSpeech: TextToSpeech? = null
    private fun initTTS(){
        textToSpeech = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech!!.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported")
                }
                textToSpeech!!.setSpeechRate(0.8f)

            } else {
                Log.e("TTS", "Initialization failed")
            }
        }
    }
    private fun speak(text: String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
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
                    showInputDialog()
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
    private val barcodeRegisterForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            val data: Intent? = result.data
            if(data != null){
                val barcode = data.getStringExtra("SCAN_RESULT")
                inputHNDialog!!.setInputHN(barcode!!)
                Toast.makeText(this, "Register HN: ${barcode}", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, "Failure", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun showInputDialog() {
        inputHNDialog = InputHNDialog(this, barcodeRegisterForResult)
        inputHNDialog!!.setEvent { hn ->
            setRegisterLocker(hn)
        }
        inputHNDialog!!.show()
    }

    private fun setRegisterLocker(hn: String){
        if (hn.isBlank()) {
            Toast.makeText(this, "Please enter HN number", Toast.LENGTH_SHORT).show()
            return
        }

        //OPEN DRAWER
        if(drawer1List.any { it.hn == hn }){
            Toast.makeText(this, "This HN number already exists.", Toast.LENGTH_SHORT).show()
            inputHNDialog!!.setShowErrorInput(true)
            return
        }

        lastInputHN = hn
        unlockLocker(lastPosition)

        inputHNDialog!!.dismiss()
        showUnlockDialog()


    }

    private var disconnectDialog: AlarmDisconnectDialog? = null
    private fun showDisconnectDialog(){
        if(disconnectDialog == null){
            disconnectDialog = AlarmDisconnectDialog(this, bwDevice)
        }
        disconnectDialog!!.show()
    }
    private fun hideDisconnectDialog(){
        disconnectDialog?.dismiss()
    }


    private fun showSuccessDialog(){
        val dialog = AlarmSuccessDialog(this)
        dialog.show()
    }

    private var currentPositionVerify = -1
    private var unlockDialog: AlarmUnlockDialog? = null
    private fun showUnlockDialog(){

        if(unlockDialog == null){//init
            unlockDialog = AlarmUnlockDialog(this)
            unlockDialog!!.setTitle("Locker is unlock")
            unlockDialog!!.setDescription("Put the pills in the drawer.")
            unlockDialog!!.setOnDismissListener {
                updateData()
                Toast.makeText(this, "Locker is lock.", Toast.LENGTH_SHORT).show()
            }
        }

        unlockDialog!!.setTitle("Locker is unlock")
        if(unlockDialog!!.isShowing){// if showing
            Timer().schedule(1000) {
                Log.i(TAG, "Timer.")
                bwDevice.checkStatusLockerAll()
            }
        }else{ //if not showing
            val locker = drawer1List[lastPosition]
            locker.hn = lastInputHN
            unlockDialog!!.setModel(locker)
            unlockDialog!!.show()

            Timer().schedule(600) {
                Log.i(TAG, "Register Timer is Check Status Locker")
                bwDevice.checkStatusLockerAll()
            }
            Toast.makeText(this, "Locker is unlock.", Toast.LENGTH_SHORT).show()
        }

    }
    private fun hideUnlockDialog(){
        unlockDialog?.dismiss()
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