/*
 * C()opyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rc.wellnestmodule

import android.app.Service
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.rc.wellnestmodule.interfaces.ISendMessageToEcgDevice
import com.rc.wellnestmodule.models.BleOperationType
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
open class BluetoothLeService : Service() {

    var isV4: Boolean = false

    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDeviceAddress: String? = null
    private var mBluetoothGatt: BluetoothGatt? = null
//    val characteristicUUID = "0000ffe1-0000-1000-8000-00805f9b34fb"

    var bleWriteCharacteristics: BluetoothGattCharacteristic? = null
    private var bleReadCharacteristics: BluetoothGattCharacteristic? = null
    private val MAX_MTU: Int = 512
    var isBluetoothLiveRecording = false

    private val operationQueue = ConcurrentLinkedQueue<BleOperationType>()
    private var pendingOperation: BleOperationType? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service is onCreate")
        /* On some devices bluetooth state change is not notified through gatt callback
           so instead register for bluetooth state change via broadcast receiver this is guaranteed to
           be received on every device.
         */
        registerReceiver(stateChangeListener, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    private val stateChangeListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state =
                    intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
                if (state == BluetoothAdapter.STATE_OFF) {
                    this@BluetoothLeService.disconnect()
                    this@BluetoothLeService.mBluetoothGatt?.close()
                    this@BluetoothLeService.mBluetoothGatt = null
                    broadcastUpdate(ACTION_GATT_DISCONNECTED)
                }
            }
        }
    }

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
////        Toast.makeText(getApplicationContext(),"CALL YOUR METHOD",Toast.LENGTH_LONG).show();
//        Log.i(TAG, "Service is onStartCommand")
//        return START_STICKY
//    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private val mGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            var intentAction: String

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    intentAction = ACTION_GATT_CONNECTED
                    broadcastUpdate(intentAction)
                    Log.i(TAG, "Connected to GATT server.")
                    Log.i(
                        TAG,
                        "Attempting to start service discovery:" + mBluetoothGatt!!.discoverServices()
                    )

                    if (pendingOperation is BleOperationType.Connect) {
                        signalEndOfOperation()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    intentAction = ACTION_GATT_DISCONNECTED
                    mBluetoothGatt?.close()
                    mBluetoothGatt = null
                    Log.i(TAG, "Disconnected from GATT server.")
                    broadcastUpdate(intentAction)
                    if (pendingOperation is BleOperationType.Disconnect) {
                        signalEndOfOperation()
                    }
                }
            } else {
                when (status) {
                    8 -> {
                        // connection lost
                        intentAction = ACTION_GATT_DISCONNECTED
                        mBluetoothGatt?.close()
                        mBluetoothGatt = null
                        broadcastUpdate(intentAction)
                        signalEndOfOperation()
                        pendingOperation = null
                        operationQueue.clear()
                    }
                    133 -> {
                        // connection timeout
                        intentAction = ACTION_GATT_DISCONNECTED
                        mBluetoothGatt?.close()
                        mBluetoothGatt = null
                        broadcastUpdate(intentAction)
                        signalEndOfOperation()
                        pendingOperation = null
                        operationQueue.clear()
                    }
                    else -> {
                        intentAction = ACTION_GATT_DISCONNECTED
                        mBluetoothGatt?.close()
                        mBluetoothGatt = null
                        broadcastUpdate(intentAction)
                        signalEndOfOperation()
                        pendingOperation = null
                        operationQueue.clear()
                    }
                }

            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {


            if (status == BluetoothGatt.GATT_SUCCESS) {

                val services = gatt.services ?: return

                for (service in services) {
                    service.characteristics.forEach { characteristic ->
                        if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) {
                            bleWriteCharacteristics = characteristic
                        }

                        if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
                            bleReadCharacteristics = characteristic
                        }
                    }
                }

                enqueueOperation(BleOperationType.MtuRequest(mBluetoothGatt ?: return, MAX_MTU))
//                if (!gatt.requestMtu(MAX_MTU)) {
//                    Log.v(TAG,"Request MTU Failed!!")
//                    connectCharacteristicNotification()
//                }
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            Log.v(TAG, "Received : ${String(characteristic.value)}")
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            if (status == 0) {
                iSendMessageToEcgDevice.sendMessage(bleWriteCharacteristics!!, 0)
            }
            if (pendingOperation is BleOperationType.DescriptorWrite) {
                signalEndOfOperation()
            }
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorRead(gatt, descriptor, status)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (pendingOperation is BleOperationType.CharacteristicsWrite) {
                signalEndOfOperation()
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.v(TAG, "MTU Changed : $mtu")
            } else {
                Log.v(TAG, "Error Changing MTU")
            }
            connectCharacteristicNotification()

            if (pendingOperation is BleOperationType.MtuRequest) {
                signalEndOfOperation()
            }
        }
    }

    fun sendingMessage(characteristic: BluetoothGattCharacteristic, message: String) {
        Log.i(TAG, message + "\n")
        characteristic.setValue(message)
        writeCharacteristic(characteristic)
    }

    fun sendMessage(
        message: String,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
    ) {
        enqueueOperation(
            BleOperationType.CharacteristicsWrite(
                mBluetoothGatt ?: return,
                message,
                writeType
            )
        )
        Log.i(TAG, "Sending " + message + "\n")
//        bleWriteCharacteristics!!.writeType = writeType
//
//        bleWriteCharacteristics!!.setValue(message)
//        if (mBluetoothGatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized")
//            return
//        }
//        mBluetoothGatt!!.writeCharacteristic(bleWriteCharacteristics!!)
    }

    private val mBinder = LocalBinder()

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after `BluetoothGatt#discoverServices()` completes successfully.
     *
     * @return A `List` of supported services.
     */
    val supportedGattServices: List<BluetoothGattService>?
        get() = if (mBluetoothGatt == null) null else mBluetoothGatt!!.services

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun broadcastUpdate(
        action: String,
        characteristic: BluetoothGattCharacteristic
    ) {
        val intent = Intent(action)
        val data = characteristic.value
        if (data != null && data.size > 0) {
            intent.putExtra(EXTRA_DATA, data)
        }
        sendBroadcast(intent)
    }

    inner class LocalBinder : Binder() {
        val service: BluetoothLeService
            get() = this@BluetoothLeService


    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close()
        return super.onUnbind(intent)
    }

    private lateinit var iSendMessageToEcgDevice: ISendMessageToEcgDevice

    fun setSendingMessageCallbacks(iSendMessageToEcgDevice: ISendMessageToEcgDevice) {
        this.iSendMessageToEcgDevice = iSendMessageToEcgDevice
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    fun initialize(): Boolean {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.")
                return false
            }
        }

        mBluetoothAdapter = mBluetoothManager!!.adapter
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }

        return true
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun connect(address: String?): Boolean {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.")
            return false
        }


        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.")
            return false
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        enqueueOperation(BleOperationType.Connect(device, this))
//        device.connectGatt(this, false, mGattCallback)
        Log.d(TAG, "Trying to create a new connection.")
        mBluetoothDeviceAddress = address
        return true
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        enqueueOperation(BleOperationType.Disconnect(mBluetoothGatt ?: return))
        Log.i(TAG, "disconnected\n")
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    fun close() {
        if (mBluetoothGatt == null) {
            return
        }
//        mBluetoothGatt!!.close()
//        mBluetoothGatt = null
        enqueueOperation(BleOperationType.Disconnect(mBluetoothGatt ?: return))
    }

    /**
     * Request a read on a given `BluetoothGattCharacteristic`. The read result is reported
     * asynchronously through the `BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)`
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        Log.v(TAG, "readCharacteristic: " + characteristic.uuid.toString())
        mBluetoothGatt!!.readCharacteristic(characteristic)
    }


    /**
     * Requst a write on a give `BluetoothGattCharacteristic`. The write result is reported
     * asynchronously through the `BluetoothGattCallback#onCharacteristicWrite(andorid.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)`
     * callback.
     */
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        Log.v(TAG, "writeCharacteristic: " + characteristic.uuid.toString())
        enqueueOperation(
            BleOperationType.CharacteristicsWrite(
                mBluetoothGatt ?: return,
                characteristic.getStringValue(0),
                characteristic.writeType
            )
        )
//        mBluetoothGatt!!.writeCharacteristic(characteristic)

    }


    companion object {
        val TAG = BluetoothLeService::class.java.simpleName

        val STATE_DISCONNECTED = 0
        val STATE_CONNECTING = 1
        val STATE_CONNECTED = 2

        val ACTION_GATT_CONNECTED = "com.wellnest.bluetooth.le.ACTION_GATT_CONNECTED"
        val ACTION_GATT_DISCONNECTED = "com.wellnest.bluetooth.le.ACTION_GATT_DISCONNECTED"
        val ACTION_GATT_SERVICES_DISCOVERED =
            "com.wellnest.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        val ACTION_DATA_AVAILABLE = "com.wellnest.bluetooth.le.ACTION_DATA_AVAILABLE"
        val EXTRA_DATA = "com.wellnest.bluetooth.le.EXTRA_DATA"
        val ACTION_CHARACTERISTIC_WRITTEN =
            "com.wellnest.bluetooth.le.ACTION_CHARACTERISTIC_WRITTEN"
        val ACTION_DESCRIPTOR_WRITTEN = "com.wellnest.bluetooth.le.ACTION_DESCRIPTOR_WRITTEN"
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(stateChangeListener)
        disconnect()
        Log.i(TAG, "Service is onDestroy")
    }

    private fun connectCharacteristicNotification() {
        if (bleWriteCharacteristics == null || bleReadCharacteristics == null) return
        val writeProperties: Int = bleWriteCharacteristics!!.getProperties()
        if (writeProperties and BluetoothGattCharacteristic.PROPERTY_WRITE +
            BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE == 0
        ) {
            Log.v(TAG, "write characteristic not writable")
            return
        }
        if (!mBluetoothGatt?.setCharacteristicNotification(bleReadCharacteristics, true)!!) {
            Log.v(TAG, "no notification for read characteristic")
            return
        }
        val readDescriptor: BluetoothGattDescriptor =
            bleReadCharacteristics!!.descriptors[0]

        val readProperties: Int = bleReadCharacteristics!!.getProperties()

        if (readProperties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) {
            Log.d(
                TAG,
                "enable read indication"
            )
            readDescriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        } else if (readProperties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
            Log.d(
                TAG,
                "enable read notification"
            )
            readDescriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        } else {
            Log.v(TAG, "no indication/notification for read characteristic ($readProperties)")
            return
        }
        Log.d(
            TAG,
            "writing read characteristic descriptor"
        )
        enqueueOperation(BleOperationType.DescriptorWrite(mBluetoothGatt ?: return, readDescriptor))
//        if (!mBluetoothGatt?.writeDescriptor(readDescriptor)!!) {
//            Log.v(TAG, "read characteristic CCCD descriptor not writable")
//        }
    }

    fun isConnected(): Boolean {
        return mBluetoothGatt != null
    }

    @Synchronized
    private fun enqueueOperation(operation: BleOperationType) {
        Log.i("BleOperationType", "Enqueueing Operation : $operation")
        operationQueue.add(operation)
        if (pendingOperation == null) {
            doNextOperation()
        }
    }

    @Synchronized
    private fun doNextOperation() {
        if (pendingOperation != null) {
            Log.e(
                "ConnectionManager",
                "doNextOperation() called when an operation is pending! Aborting."
            )
            return
        }

        val operation = operationQueue.poll() ?: run {
            Log.v(TAG, "Operation queue empty, returning")
            return
        }
        pendingOperation = operation

        Log.i("BleOperationType", "Processing Operation : $operation")

        when (operation) {
            is BleOperationType.CharacteristicsRead -> {

            }
            is BleOperationType.CharacteristicsWrite -> {
                val characteristic = bleWriteCharacteristics
                characteristic?.setValue(operation.message)
                characteristic?.writeType = operation.writeType
                operation.bluetoothGatt.writeCharacteristic(characteristic)
            }
            is BleOperationType.Connect -> {
                mBluetoothGatt =
                    operation.device.connectGatt(operation.context, false, mGattCallback)
            }
            is BleOperationType.DescriptorRead -> {
            }
            is BleOperationType.DescriptorWrite -> {
                if (!operation.bluetoothGatt.writeDescriptor(operation.descriptor)) {
                    Log.v(TAG, "read characteristic CCCD descriptor not writable")
                }
            }
            is BleOperationType.MtuRequest -> {
                if (!operation.bluetoothGatt.requestMtu(operation.mtuSize)) {
                    connectCharacteristicNotification()
                }
            }
            is BleOperationType.Disconnect -> {
                operation.bluetoothGatt.disconnect()
            }
        }
    }

    @Synchronized
    private fun signalEndOfOperation() {
        Log.d("ConnectionManager", "End of $pendingOperation")
        pendingOperation = null
        if (operationQueue.isNotEmpty()) {
            doNextOperation()
        }
    }
}
