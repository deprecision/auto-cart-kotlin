package com.example.smartdrugcart

import java.util.regex.Pattern

const val KEY_CONNECT = "connect"
const val KEY_DISSCONNET = "disconnect"

const val KEY_UNLOCK = "Unlock"
const val KEY_LOCK = "Lock"
const val KEY_PAUSE = "Pause"

const val KEY_MODE_REGISTER = "Register"
const val KEY_MODE_PAY = "Pay"

const val KEY_EVENT_CMD_UNLOCK = "unlock"
fun isValidMacAddress(macAddress: String?): Boolean {
    val macPattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"
    return Pattern.matches(macPattern, macAddress)
}