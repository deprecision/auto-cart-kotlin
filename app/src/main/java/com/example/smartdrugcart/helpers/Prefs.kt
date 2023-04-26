package com.example.smartdrugcart.helpers

import android.content.Context
import android.content.SharedPreferences

class Prefs (private var context: Context) {

    private var preferences: SharedPreferences = context.getSharedPreferences("Setting", Context.MODE_PRIVATE)

    //mac address
    private val APP_PREF_STR_MAC_ADDRESS = "strMacAddress"
    var strMacAddress: String?
        get() = preferences.getString(APP_PREF_STR_MAC_ADDRESS, null)
        set(value) = preferences.edit().putString(APP_PREF_STR_MAC_ADDRESS, value).apply()

}