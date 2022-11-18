package com.rc.wellnestmodule.interfaces

import android.bluetooth.BluetoothGattCharacteristic

interface ISendMessageToEcgDevice {
    fun sendMessage(characteristic: BluetoothGattCharacteristic, i: Int)
}