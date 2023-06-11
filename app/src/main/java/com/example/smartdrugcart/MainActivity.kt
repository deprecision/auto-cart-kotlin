package com.example.smartdrugcart

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
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

    private var lastPosition = -1

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Log.i(TAG, "onCreate")

        //DD:65:0C:D3:9A:02

        init()
        event()

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        addDataList()
        bwDevice.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        bwDevice.destroy()

    }

    private fun init(){

        initUnlockDialog()
        openingDialog = OpeningDialog(this)

        prefs = Prefs(this)
        functions = FunctionsLocker(this)
        initDevice()

        binding.macAddressTV.text = prefs.strMacAddress
        binding.stateDeviceTV.text = KEY_DISSCONNET
        binding.stateDeviceTV.setTextColor(ContextCompat.getColor(this, R.color.colorRed))
        binding.stateDeviceIV.setColorFilter(ContextCompat.getColor(this, R.color.colorRed), PorterDuff.Mode.SRC_ATOP)

        lastPosition = prefs.intLastPosition
        showDisconnectDialog()
    }

    private fun initDevice(){
        bwDevice = BwDevice(this)
        bwDevice.setMyEvent{ event ->
            when(event){
                BwDevice.STATE_CONNECTED->{
                    updateUI(BwDevice.STATE_CONNECTED)
                }
                BwDevice.STATE_DISCONNECTED->{
                    updateUI(BwDevice.STATE_DISCONNECTED)
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
                            showClearDialog(lastPosition)
                            Toast.makeText(this, "Locker is lock.", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            }
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
    private fun showInputDialog(){
        inputHNDialog = InputHNDialog(this)
        inputHNDialog!!.setEvent { hn->
            setPay(hn)
        }
        inputHNDialog!!.show()
    }

    private fun showPasswordDialog(){
        val dialog = PasswordDialog(this)
        dialog.setEvent {
            if(it == "1111"){
                var intent = Intent(this, SettingMacAddressActivity::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(this, "invalid password.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }


    private var inputUserIDDialog: InputUserIDDialog? = null
    private fun showInputUserDialog(){
        inputUserIDDialog = InputUserIDDialog(this)
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

    private fun setPay(hn: String){

        if(hn.isBlank()){
            inputHNDialog!!.setShowErrorInput(true, "*Please enter HN number.")
            Toast.makeText(this, "Please enter hn number", Toast.LENGTH_SHORT).show()
            return
        }

        val position = drawer1List.indexOfFirst { it.hn == hn}
        if(position != -1){
            lastPosition = position
            inputHNDialog!!.dismiss()

            showOpeningDialog(position)
        }else{
            inputHNDialog!!.setShowErrorInput(true, "Not found")
            Toast.makeText(this, "Not found", Toast.LENGTH_SHORT).show()
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

    private var countUnlock = 0
    private lateinit var unlockDialog: UnlockDialog
    private fun initUnlockDialog(){
        unlockDialog = UnlockDialog(this)
        unlockDialog.setTitle("Locker is unlock")
        unlockDialog.setDescription("Take the pills out of the drawer.")
        unlockDialog.setMyDismiss { event ->
            when(event){
                UnlockDialog.EVENT_SKIP->{
                    Log.i(TAG, "EVENT_SKIP")

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
        Log.i(TAG, "showUnlockDialog(${position})")
        var number = position + 1
        unlockDialog.setTitle("Locker is unlock")
        unlockDialog.setNumber(number.toString())
        unlockDialog.show()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable{
            override fun run() {

                if(unlockDialog.isShowing){
                    bwDevice.checkStatusLockerAll()
                    Log.i(TAG, "Check status drawer.")
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
        openingDialog!!.setModel(drawer1List[lastPosition])
        openingDialog!!.show()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable{
            override fun run() {

                if(openingDialog.isShowing){

                    if(i == 3){
                        i = 0
                        hideOpeningDialog()
                        Toast.makeText(this@MainActivity, "try again.", Toast.LENGTH_SHORT).show()
                        return
                    }
                    i++

                    unlockLocker(position)
                    Log.i(TAG, "Unlock drawer.")

                    Thread.sleep(1000)
                    bwDevice.checkStatusLockerAll()
                    Log.i(TAG, "Check status drawer.")
                    handler.postDelayed(this, 1000)
                }

            }
        }, 0)

    }
    private fun hideOpeningDialog(){
        openingDialog?.dismiss()
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

    private fun unlockLocker(position: Int){
        val locker = drawer1List[position]
        bwDevice.sendCommand(locker.position!!.toInt(), locker.drawerAt!!.toInt(), locker.cmdUnlock!!)
    }


}