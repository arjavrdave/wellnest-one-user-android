package com.rc.wellnestmodule.utils

import android.content.Context
import com.rc.wellnestmodule.models.ECGDevice

internal object SharedPreference {
    val instance = SharedPreference

    /**
     * TODO
     *
     * @param bluetoothDevice
     * @param context
     */
    fun saveBleDevice(bluetoothDevice:ECGDevice, context: Context){
        val prefer = context.getSharedPreferences("WELLNEST", Context.MODE_PRIVATE)
        val editor = prefer.edit()
        editor.putString("bluetoothDevice", Util.instance.gsonInstance.toJson(bluetoothDevice))
        editor.apply()
    }

    /**
     * TODO
     *
     * @param context
     * @return
     */
    fun getBluetoothDevice(context: Context): ECGDevice? {
        val prefer = context.getSharedPreferences("WELLNEST", Context.MODE_PRIVATE)
        val preferString = prefer.getString("bluetoothDevice", null)

        return Util.instance.gsonInstance.fromJson(preferString, ECGDevice::class.java)
    }

    /**
     * TODO
     *
     * @param context
     * @param key
     */
    fun clearKey(context: Context, key:String){
        val prefer = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefer.edit()
        editor.remove(key)
        editor.clear()
        editor.apply()
    }
}
