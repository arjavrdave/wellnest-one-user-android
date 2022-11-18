package com.rc.wellnestmodule.interfaces

import android.bluetooth.BluetoothDevice

interface IBluetooth {
    fun connect(bluetoothDevice: BluetoothDevice)
    fun disConnect()
}