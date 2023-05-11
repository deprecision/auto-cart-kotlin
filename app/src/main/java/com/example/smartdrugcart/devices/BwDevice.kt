package com.example.smartdrugcart.devices
import android.app.Activity
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.smartdrugcart.helpers.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.codec.binary.Hex
import java.util.*

class BwDevice(var activity: Activity){

    companion object{
        val STATE_CONNECTED = "connected"
        val STATE_DISCONNECTED = "disconnected"
        val STATE_UNLOCK_LOGGER = "unlock"
        val STATE_LOCK_LOGGER = "lock"

        val STATE_UNLOCK = "0"
        val STATE_LOCK = "1"

        private val UUID_SERVICE = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
        private val UUID_RESULT = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")
        private val UUID_CONTROLLER = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb")
    }

    private var l: ((event: String) -> Unit?)? = null
    fun setMyEvent(l: (event: String) -> Unit) {
        this.l = l
    }

    private val TAG = "DeviceBluetoothTag"

    private var gatt: BluetoothGatt? = null
    private var device: BluetoothDevice? = null
    private var characteristic: BluetoothGattCharacteristic? = null

    private var prefs = Prefs(activity)

    //private val cmdUnlockList = arrayListOf("0200310336", "0201310337", "0202310338", "0203310339", "020431033A")
    //private val cmdCheckStateList = arrayListOf("-", "0200300335", "0201300336", "0202300337", "0203300338", "0204300339", "020530033A")

    private val bluetoothManager: BluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter

    fun connect() {
        //DD:65:0C:D3:9A:02
        if(gatt != null){
            return
        }

        val macAddress = prefs.strMacAddress
        device = bluetoothAdapter.getRemoteDevice(macAddress)
        gatt = device!!.connectGatt(activity, true, gattCallback)
    }

    fun reconnect(){
        gatt?.close()
        gatt = null

        val macAddress = prefs.strMacAddress
        device = bluetoothAdapter.getRemoteDevice(macAddress)
        gatt = device!!.connectGatt(activity, true, gattCallback)
    }

    fun isConnect(): Boolean{
        return gatt != null
    }

    private var lastPosition = -1
    private var lastDrawerAt = -1
    private var currentCmd = ""
    private val cmdCheckState = "02 F0 32 03 27"  //check state all
    fun sendCommand(position: Int, drawerAt: Int, cmd: String) {
        if (characteristic == null || gatt == null) {
            Toast.makeText(activity, "Bluetooth is disconnect", Toast.LENGTH_SHORT).show()
            return
        }

        lastPosition = position
        lastDrawerAt = drawerAt
        currentCmd = cmd

        writeCharacteristicCurrent(currentCmd)
    }

    fun checkStatusLockerAll(){
        writeCharacteristicCurrent(cmdCheckState)
    }

    fun destroy(){
        gatt?.close()
        gatt = null
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            val rootView: View = activity.window.decorView.rootView
            Log.i(TAG, "onConnectionStateChange: $newState")
            rootView.post {
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        gatt?.discoverServices()
                        l?.let { it(STATE_CONNECTED) }

                        Log.i(TAG, "STATE_CONNECTED")
                        Log.i(TAG, "discoverServices")
                    }
                    BluetoothGatt.STATE_DISCONNECTED -> {
                        l?.let { it(STATE_DISCONNECTED) }
                        Log.i(TAG, "STATE_DISCONNECTED")
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            characteristic = gatt!!.getService(UUID_SERVICE).getCharacteristic(UUID_CONTROLLER)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.i(TAG, "onServicesDiscovered GATT_SUCCESS ")
                    try {
                        val scope = CoroutineScope(SupervisorJob())
                        scope.launch {

                            //set notify
                            val characterResult = gatt.getService(UUID_SERVICE).getCharacteristic(UUID_RESULT)
                            gatt.setCharacteristicNotification(characterResult, true)

                            val descriptor = characterResult.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                            delay(500)

                            setPassword()
                            delay(500)

                            setTimeDisconnect()
                            delay(1000)

                            //check status of logger
//                            val handler = Handler(Looper.getMainLooper())
//                            handler.postDelayed(object : Runnable{
//                                override fun run() {
//                                    if(gatt != null && characteristic != null){ //02 F0 32 03 27
//                                        val checkAllState = Hex.decodeHex("02 F0 32 03 27".replace(" ", "").toCharArray())
//                                        characteristic!!.value = checkAllState
//                                        characteristic!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
//                                        gatt.writeCharacteristic(characteristic!!)
//
//                                        handler.postDelayed(this, 1000)
//                                    }
//                                }
//                            }, 1000)

                        }
                    } catch (e: Exception) {
                        Log.i(TAG, "ERROR: $e")
                    }
                }
                else -> Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            Log.i(TAG, "onCharacteristicRead")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.i(TAG, "onCharacteristicRead status: GATT_SUCCESS")
                    broadcastUpdate(characteristic)
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.i(TAG, "onCharacteristicChanged: ")
            broadcastUpdate(characteristic!!)

        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.i(TAG, "onDescriptorWrite : ${descriptor.uuid}")
        }
    }
    private fun setPassword(){
        //set password (0x60 = set pairing password)
        val password = "9C 60 00 04 C9 A9 38 38 38 38 38".replace(" ", "")
        writeCharacteristicCurrent(password)
        Log.i(TAG, "Verify Password. ")
    }

    private fun setTimeDisconnect(){
        //set time disconnect. (67 = set time disconnect, FE = time is unlimited) set one time.
        val cmdSetTimeDisconnect = "9C 67 00 01 C9 CB FE".replace(" ", "")
        writeCharacteristicCurrent(cmdSetTimeDisconnect)
    }

    private fun broadcastUpdate(characteristic: BluetoothGattCharacteristic) {
        Log.i(TAG, "broadcastUpdate : ${characteristic.uuid}")

        when (characteristic.uuid) {
            UUID_RESULT -> {
                val data: ByteArray? = characteristic.value
                if (data?.isNotEmpty() == true) {

                    val hexString: String = data.joinToString(separator = " ") { String.format("%02X", it) }
                    Log.i(TAG, "UUID_RESULT data hex: $hexString")

                    when("${hexString[0]}${hexString[1]}"){
                        "9C"->{

                        }
                        "02"->{//check status locker
                            val hexNumber = "${hexString[9]}${hexString[10]}" // Replace with your hex number
                            val decimalNumber = hexNumber.toInt(16) // Convert hex to decimal
                            val binaryNumber = String.format("%8s", Integer.toBinaryString(decimalNumber)).replace(' ', '0')

                            Log.i(TAG, "hexNumber: $hexNumber")
                            Log.i(TAG, "binaryNumber: $binaryNumber")

                            Log.i(TAG, "lastPosition: $lastPosition")

                            val statusLocker = binaryNumber[8 - lastPosition].toString()
                            when(statusLocker){
                                STATE_UNLOCK->{
                                    Log.i(TAG, "statusCurrentLocker: STATE_UNLOCK")

                                    val rootView: View = activity.window.decorView.rootView
                                    rootView.post {
                                        l?.let { it(STATE_UNLOCK_LOGGER) }//check value againt
                                    }
                                }
                                STATE_LOCK->{
                                    Log.i(TAG, "statusCurrentLocker: STATE_LOCK")

                                    val rootView: View = activity.window.decorView.rootView
                                    rootView.post {
                                        l?.let { it(STATE_LOCK_LOGGER) }//check value againt
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                // For all other profiles, writes the data formatted in HEX.
                val data: ByteArray? = characteristic.value
                if (data?.isNotEmpty() == true) {
                    val hexString: String = data.joinToString(separator = " ") { String.format("%02X", it) }
                    Log.i(TAG, "ValueEmpty: $hexString")
                }
            }
        }
    }

    private fun writeCharacteristicCurrent(str: String){
        val command = Hex.decodeHex(str.replace(" ", "").toCharArray())
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                gatt!!.writeCharacteristic(
                    characteristic!!,
                    command,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )
            }else{
                characteristic!!.value = command
                characteristic!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                gatt!!.writeCharacteristic(characteristic)
            }
        }catch (e: java.lang.NullPointerException){
            Log.i(TAG, "gatt == null")
        }
    }
}

//check service
//val servicesList = gatt!!.services
//for (s in servicesList){
//    Log.i(TAG, "service: " + s.uuid)
//    for (c in s.characteristics){
//        Log.i(TAG, "c: " + c.uuid)
//    }
//}



