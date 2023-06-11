package com.example.smartdrugcart.devices
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.content.Context
import android.os.Handler
import android.os.Looper
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
import kotlin.random.Random

@SuppressLint("MissingPermission")
class BwDevice(var activity: Activity){

    companion object{
        val STATE_CONNECTED = "connected"
        val STATE_DISCONNECTED = "disconnected"
        val STATE_UNLOCK_LOCKER = "unlock"
        val STATE_LOCK_LOCKER = "lock"

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

    private val bluetoothManager: BluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter

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

    fun destroy(){
        gatt?.close()
        gatt = null
        lastPosition = -1
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            val rootView: View = activity.window.decorView.rootView
            rootView.post {
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        gatt?.discoverServices()
                        l?.let { it(STATE_CONNECTED) }
                    }
                    BluetoothGatt.STATE_DISCONNECTED -> {
                        l?.let { it(STATE_DISCONNECTED) }
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            characteristic = gatt!!.getService(UUID_SERVICE).getCharacteristic(UUID_CONTROLLER)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {

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

                            if(lastPosition != -1){
                                checkStatusLockerAll()
                            }

                        }
                    } catch (e: Exception) {

                    }
                }
                else -> Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    broadcastUpdate(characteristic)
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            broadcastUpdate(characteristic!!)

        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
        }
    }
    private fun setPassword(){
        //set password (0x60 = set pairing password)
        val password = "9C 60 00 04 C9 A9 38 38 38 38 38".replace(" ", "")
        writeCharacteristicCurrent(password)
    }

    private fun setTimeDisconnect(){
        //set time disconnect. (67 = set time disconnect, FE = time is unlimited) set one time.
        val cmdSetTimeDisconnect = "9C 67 00 01 C9 CB FE".replace(" ", "")
        writeCharacteristicCurrent(cmdSetTimeDisconnect)
    }

    private fun broadcastUpdate(characteristic: BluetoothGattCharacteristic) {

        when (characteristic.uuid) {
            UUID_RESULT -> {
                val data: ByteArray? = characteristic.value
                if (data?.isNotEmpty() == true) {

                    val hexString: String = data.joinToString(separator = " ") { String.format("%02X", it) }
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
                            val rootView: View = activity.window.decorView.rootView

                            val statusLocker = binaryNumber[8 - lastPosition].toString()
                            when(statusLocker){
                                STATE_UNLOCK->{
                                    rootView.post {
                                        l?.let { it(STATE_UNLOCK_LOCKER) }//check value againt
                                    }
                                }
                                STATE_LOCK->{
                                    rootView.post {
                                        l?.let { it(STATE_LOCK_LOCKER) }//check value againt
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
                }
            }
        }
    }

    private fun writeCharacteristicCurrent(str: String){
        if(gatt == null){
            val rootView: View = activity.window.decorView.rootView
            rootView.post {
                Toast.makeText(activity, "Gatt disconnect", Toast.LENGTH_SHORT).show()
            }
            return
        }

        try {
            val command = Hex.decodeHex(str.replace(" ", "").toCharArray())
            characteristic!!.value = command
            characteristic!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            gatt!!.writeCharacteristic(characteristic)

        }catch (e: java.lang.NullPointerException){
            Log.i(TAG, "gatt == null")
        }
    }
}



