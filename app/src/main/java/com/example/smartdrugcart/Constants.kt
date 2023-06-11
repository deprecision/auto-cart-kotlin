package com.example.smartdrugcart

import android.os.Handler
import android.os.Looper
import java.util.regex.Pattern

const val KEY_CONNECT = "connect"
const val KEY_DISSCONNET = "disconnect"

const val KEY_UNLOCK = "Unlock"

const val KEY_ENABLE = "Enable"
const val KEY_DISABLE = "Disable"
const val KEY_PAUSE = "Pause"


const val KEY_MODE_REGISTER = "Register"
const val KEY_MODE_PAY = "Pay"

const val KEY_EVENT_CMD_UNLOCK = "unlock"
fun isValidMacAddress(macAddress: String?): Boolean {
    val macPattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"
    return Pattern.matches(macPattern, macAddress)
}

fun postDelayIf(repeatFunc: () -> Unit, delayMillisec: Long, condition: () -> Boolean){
    val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable{
            override fun run() {

                if(condition()){
                    repeatFunc()

                    handler.postDelayed(this, delayMillisec)
                }
            }
        }, delayMillisec)
}