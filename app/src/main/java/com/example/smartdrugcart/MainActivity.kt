package com.example.smartdrugcart

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.healthmessage.database.FunctionsLocker
import com.example.smartdrugcart.adapters.AdapterLocker
import com.example.smartdrugcart.databinding.ActivityMainBinding
import com.example.smartdrugcart.devices.BwDevice
import com.example.smartdrugcart.dialogs.*
import com.example.smartdrugcart.helpers.Prefs
import com.example.smartdrugcart.models.ModelLocker
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivityTag"

    private lateinit var prefs: Prefs
    private lateinit var functions: FunctionsLocker
    //dialogs
    private lateinit var bwDevice: BwDevice
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val drawer1List = ArrayList<ModelLocker>()
    private val drawer2List = ArrayList<ModelLocker>()

    private var lastPosition = 0

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Log.i(TAG, "onCreate")

        //DD:65:0C:D3:9A:02

        init()
        initTTS()
        event()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        addDataList()
        bwDevice.connect()

        Log.i(TAG, "onResume")

    }

    override fun onDestroy() {
        super.onDestroy()
        bwDevice.destroy()

        Log.i(TAG, "onDestroy")
    }

    private fun init(){

        prefs = Prefs(this)
        functions = FunctionsLocker(this)
        bwDevice = BwDevice(this)
        bwDevice.setMyEvent{ event ->
            when(event){
                BwDevice.STATE_CONNECTED->{
                    Log.i(TAG, "Main STATE_CONNECTED")
                    updateUI(BwDevice.STATE_CONNECTED)
                }
                BwDevice.STATE_DISCONNECTED->{
                    Log.i(TAG, "Main STATE_DISCONNECTED")
                    updateUI(BwDevice.STATE_DISCONNECTED)
                    bwDevice.reconnect()
                }
                BwDevice.STATE_UNLOCK_LOGGER->{
                    if(openingDialog?.isShowing == true){
                        hideOpeningDialog()
                    }

                    showUnlockDialog()
                    if(currentPositionVerify != -1){
                        currentPositionVerify = -1   //reset value
                        speak("Please close the drawer completely.")
                    }
                    Log.i(TAG, "Main STATE_UNLOCK_LOGGER")
                }
                BwDevice.STATE_LOCK_LOGGER->{
                    if(openingDialog?.isShowing == true){
                        hideOpeningDialog()
                        //สั่งเปิดเเต่ไม่เปิดลิ้นชัก
                        //showReportDialog()
                        Snackbar.make(binding.root, "Locker is lock.", Toast.LENGTH_LONG).show()
                        return@setMyEvent
                    }

                    if(lastPosition == currentPositionVerify){
                        hideUnlockDialog()
                        currentPositionVerify = -1 //reset value
                        Toast.makeText(this, "Locker is lock.", Toast.LENGTH_SHORT).show()
                        Log.i(TAG, "Main STATE_LOCK_LOGGER")
                    }else{
                        currentPositionVerify = lastPosition //set for check
                        unlockDialog!!.setTitle("Checking drawer status...")
                        Timer().schedule(3000) {
                            Log.i(TAG, "Timer.")
                            bwDevice.checkStatusLockerAll()
                        }
                        Log.i(TAG, "Main STATE_LOCK_LOGGER Checking...")
                    }
                }
            }
        }

        binding.macAddressTV.text = prefs.strMacAddress
        binding.stateDeviceTV.text = KEY_DISSCONNET
        binding.stateDeviceTV.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
        binding.stateDeviceIV.setColorFilter(ContextCompat.getColor(this, R.color.colorRed), PorterDuff.Mode.SRC_ATOP)

        lastPosition = prefs.intLastPosition
        showDisconnectDialog()
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

    private fun updateUI(status: String){
        when(status){
            BwDevice.STATE_CONNECTED->{
                binding.stateDeviceTV.text = KEY_CONNECT
                binding.stateDeviceTV.setTextColor(ContextCompat.getColor(this, R.color.colorGreen))
                binding.stateDeviceIV.setColorFilter(ContextCompat.getColor(this, R.color.colorGreen), PorterDuff.Mode.SRC_ATOP)
                hideDisconnectDialog()
            }
            BwDevice.STATE_DISCONNECTED->{
                binding.stateDeviceTV.text = KEY_DISSCONNET
                binding.stateDeviceTV.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
                binding.stateDeviceIV.setColorFilter(ContextCompat.getColor(this, R.color.colorRed), PorterDuff.Mode.SRC_ATOP)
                showDisconnectDialog()
            }
        }
    }

    private fun addDataList(){
        drawer1List.clear()
        drawer2List.clear()

        drawer1List.addAll(functions.getDataListDrawerAt("1"))
        drawer2List.addAll(functions.getDataListDrawerAt("2"))

        adapter()
    }

    private fun adapter(){
        val adapter1 = AdapterLocker(this, drawer1List, KEY_MODE_PAY)
        val layoutManager = GridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false)
        binding.drawer1RCV.adapter = adapter1
        binding.drawer1RCV.layoutManager = layoutManager

        val adapter2 = AdapterLocker(this, drawer2List, "")
        val layoutManager2 = GridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false)
        binding.drawer2RCV.adapter = adapter2
        binding.drawer2RCV.layoutManager = layoutManager2

    }

    private fun event(){

        binding.settingIV.setOnClickListener {
            showPasswordDialog()
        }

        binding.registerLL.setOnClickListener {
            openRegister()
        }

        binding.payLL.setOnClickListener {
            showInputDialog()
        }

    }

    private fun openRegister(){
        showInputUserDialog()
    }

    private var inputHNDialog: InputHNDialog? = null
    private val barcodeForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            Log.i(TAG, "Main barcodeForResult")

            val data: Intent? = result.data
            if(data != null){
                val barcode = data?.getStringExtra("SCAN_RESULT")
                inputHNDialog!!.setInputHN(barcode!!)
                Toast.makeText(this, "Dispense HN: ${barcode}", Toast.LENGTH_LONG).show()

            }else{
                Toast.makeText(this, "Failure", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun showInputDialog(){

        inputHNDialog = InputHNDialog(this, barcodeForResult)
        inputHNDialog!!.setEvent { hn->
            setPay(hn)
        }
        inputHNDialog!!.show()
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


    private var inputUserIDDialog: InputUserIDDialog? = null
    private val barcodeForInputUserResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            Log.i(TAG, "Main barcodeForResult")

            val data: Intent? = result.data
            if(data != null){
                val barcode = data?.getStringExtra("SCAN_RESULT")
                inputUserIDDialog!!.setInput(barcode!!)
                Toast.makeText(this, "User ID: ${barcode}", Toast.LENGTH_LONG).show()

            }else{
                Toast.makeText(this, "Failure", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun showInputUserDialog(){
        inputUserIDDialog = InputUserIDDialog(this,  barcodeForInputUserResult)
        inputUserIDDialog!!.setEvent { userId->
            if(userId == "1234"){
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
                bwDevice.destroy()
                inputUserIDDialog!!.dismiss()
                return@setEvent
            }
            Snackbar.make(inputUserIDDialog!!.window!!.decorView, "User not found.", Snackbar.LENGTH_SHORT).show()
        }
        inputUserIDDialog!!.show()
    }

    private fun setPay(hn: String?){

        if(hn == null){
            Toast.makeText(this, "Please enter hn number", Toast.LENGTH_SHORT).show()
            return
        }

        if(hn.isBlank()){
            Toast.makeText(this, "Please enter hn number", Toast.LENGTH_SHORT).show()
            return
        }

        val locker = drawer1List.singleOrNull { it.hn == hn }
        if(locker != null){
            val position = drawer1List.indexOf(locker)
            lastPosition = position
            inputHNDialog!!.dismiss()

            bwDevice.sendCommand(locker.position!!.toInt(), locker.drawerAt!!.toInt(), locker.cmdUnlock!!)
            showOpeningDialog()
        }else{
            Toast.makeText(this, "Not found", Toast.LENGTH_SHORT).show()
        }

    }


    private var enableDialog: EnableDialog? = null
    private fun showEnableDialog(){
        enableDialog = EnableDialog(this)
        enableDialog!!.setModel(drawer1List[lastPosition])
        enableDialog!!.setEvent { event ->
            when(event){
                EnableDialog.EVENT_ENABLE->{
                    drawer1List[lastPosition].state = KEY_ENABLE
                    binding.drawer1RCV.adapter!!.notifyItemChanged(lastPosition)
                }
            }
        }
        enableDialog!!.show()
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

    private var currentPositionVerify = -1
    private var unlockDialog: UnlockDialog? = null
    private fun showUnlockDialog(){
        val locker = drawer1List[lastPosition]
        if(locker.state == KEY_DISABLE){
            return
        }

        if(unlockDialog == null){//init
            unlockDialog = UnlockDialog(this)
            unlockDialog!!.setDescription("Take the pills out of the drawer.")
            unlockDialog!!.setMyEvent { event ->
                when(event){
                    UnlockDialog.EVENT_SUCCESS->{
                        showClearDialog(lastPosition)
                        Toast.makeText(this, "Locker is lock.", Toast.LENGTH_SHORT).show()
                    }
                    UnlockDialog.EVENT_DISABLE->{
                        Log.i("fewfwe", "EVENT_DISABLE")
                        drawer1List[lastPosition].hn = null
                        drawer1List[lastPosition].counter = 0
                        drawer1List[lastPosition].state = KEY_DISABLE
                        functions.update(drawer1List[lastPosition])

                        binding.drawer1RCV.adapter?.notifyItemChanged(lastPosition)
                        hideUnlockDialog()
                    }
                }

            }
        }

        unlockDialog!!.setTitle("Locker is unlock")
        if(unlockDialog!!.isShowing){// if showing

            Timer().schedule(1000) {
                Log.i(TAG, "Timer.")
                bwDevice.checkStatusLockerAll()
            }
        }else{ //if not showing
            unlockDialog!!.setModel(locker)
            unlockDialog!!.show()

            Timer().schedule(600) {
                Log.i(TAG, "Main Timer is Check Status Locker")
                bwDevice.checkStatusLockerAll()
            }
            Toast.makeText(this, "Locker is unlock.", Toast.LENGTH_SHORT).show()
        }

    }
    private fun hideUnlockDialog(){
        unlockDialog?.dismiss()
    }

    private var openingDialog: OpeningDialog? = null
    private fun showOpeningDialog(){
        openingDialog = OpeningDialog(this)
        openingDialog!!.setModel(drawer1List[lastPosition])
        openingDialog!!.show()

        Timer().schedule(600){
            bwDevice.checkStatusLockerAll()
        }
    }
    private fun hideOpeningDialog(){
        openingDialog?.dismiss()
    }


    private var disableDialog: DisableDialog? = null
    private fun showReportDialog(){
        disableDialog = DisableDialog(this)
        disableDialog!!.show()
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


}