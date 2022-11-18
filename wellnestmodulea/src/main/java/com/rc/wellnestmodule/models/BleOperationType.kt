package com.rc.wellnestmodule.models

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import java.util.*

sealed class BleOperationType {
    companion object {
        private val TAG = BleOperationType::class.java.simpleName
    }
    data class Connect(val device: BluetoothDevice,val context: Context) : BleOperationType()
    data class CharacteristicsRead(val characteristicsUUid : UUID) : BleOperationType()
    data class CharacteristicsWrite(val bluetoothGatt: BluetoothGatt,val message : String,val writeType : Int) : BleOperationType()
    data class DescriptorRead( val descriptor : UUID) : BleOperationType()
    data class DescriptorWrite(val bluetoothGatt: BluetoothGatt,val descriptor: BluetoothGattDescriptor) : BleOperationType()
    data class MtuRequest(val bluetoothGatt: BluetoothGatt,val mtuSize : Int) : BleOperationType()
    data class Disconnect(val bluetoothGatt: BluetoothGatt) : BleOperationType()
}

