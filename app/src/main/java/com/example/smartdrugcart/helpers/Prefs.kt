package com.example.smartdrugcart.helpers

import android.content.Context
import android.content.SharedPreferences
import com.example.smartdrugcart.KEY_MODE_PAY

class Prefs (private var context: Context) {

    private var preferences: SharedPreferences = context.getSharedPreferences("Setting", Context.MODE_PRIVATE)

    //mac address
    private val APP_PREF_STR_MAC_ADDRESS = "strMacAddress"
    var strMacAddress: String?
        get() = preferences.getString(APP_PREF_STR_MAC_ADDRESS, null)
        set(value) = preferences.edit().putString(APP_PREF_STR_MAC_ADDRESS, value).apply()

    private val APP_PREF_STR_INIT_LOCKER = "strInitLocker"
    var strInitLocker: String?
        get() = preferences.getString(APP_PREF_STR_INIT_LOCKER, null)
        set(value) = preferences.edit().putString(APP_PREF_STR_INIT_LOCKER, value).apply()

    private val APP_PREF_INT_LASTPOSITION = "intLastPosition"
    var intLastPosition: Int
        get() = preferences.getInt(APP_PREF_INT_LASTPOSITION, -1)
        set(value) = preferences.edit().putInt(APP_PREF_INT_LASTPOSITION, value).apply()

}