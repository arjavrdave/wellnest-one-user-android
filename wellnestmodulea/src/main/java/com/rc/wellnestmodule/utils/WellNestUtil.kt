package com.rc.wellnestmodule.utils

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature
import com.microsoft.azure.storage.blob.CloudBlobContainer
import com.microsoft.azure.storage.blob.CloudBlockBlob
import com.rc.wellnestmodule.BluetoothLeService
import com.rc.wellnestmodule.graphview.EcgView
import com.rc.wellnestmodule.interfaces.*
import kotlinx.coroutines.*
import okhttp3.*
import java.io.File
import java.io.FileNotFoundException
import java.net.URI
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


class WellNestUtil : IWellNestUtil {

    private val TAG = WellNestUtil::class.java.simpleName
    private lateinit var iSendMessageToEcgDevice: ISendMessageToEcgDevice
    private lateinit var iWellNestData: IWellNestData
    private lateinit var iWellNestGraph: IWellnestGraph
    private lateinit var iWellnestUsbData: IWellnestUsbData
    private var mBluetoothLeService: BluetoothLeService? = null
    private var activity: Activity? = null
    private var recordingData: ByteArray = ByteArray(0)
    private val recordBytes = RecordBytesFactory().getRecordBytes()
    private var m_serviceBound = false
    private var hasPrepareRecording = false
    private var iEcgSetup: String = "standard"
    private var mUsbSerialDevice: UsbSerialDevice? = null
    private var liveRecordingStarted = false
    private var mRecordingStopped = false

    /**
     * Send the  Intent from an Activity with a custom request code
     *
     * @param activity    Activity to receive result
     * @param requestCode requestCode for result
     */
    override fun start(activity: Activity, requestCode: Int) {
//        activity.startActivityForResult(getIntent(activity), requestCode)
    }

    /**
     * Get Intent to start [DeviceConnectionActivity]
     *
     * @return Intent for [DeviceConnectionActivity]
     */
    fun getIntent(context: Activity): Intent {
        TODO()
//        val intent = Intent()
//        intent.setClass(context, DeviceConnectionActivity::class.java)
//        return intent
    }

    /*
    * Connects to the GATT server hosted on the Bluetooth LE device.
    * @param address The device address of the destination device.
    * */
    override fun connectBle(address: String) {
//        mBluetoothLeService?.let {
//            mBluetoothLeService!!.connect(address)
//        }

    }

    /*
  * start ble service*/
    override fun startBleService(
        activity: Activity,
        iWellNestGraph: IWellnestGraph,
        iWellNestData: IWellNestData,
        iSendMessageToEcgDevice: ISendMessageToEcgDevice,
        iWellnestUsbData: IWellnestUsbData
    ) {
        this.activity = activity
        this.iWellNestData = iWellNestData
        this.iWellNestGraph = iWellNestGraph
        this.iSendMessageToEcgDevice = iSendMessageToEcgDevice
        this.iWellnestUsbData = iWellnestUsbData
        val gattServiceIntent = Intent(activity, BluetoothLeService::class.java)
        //activity.startService(gattServiceIntent)
        activity.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
        m_serviceBound = true
    }

    // Code to manage Service lifecycle.
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBluetoothLeService = (service as BluetoothLeService.LocalBinder).service
            mBluetoothLeService!!.setSendingMessageCallbacks(iSendMessageToEcgDevice)
            if (!mBluetoothLeService!!.initialize()) {
                Log.i(TAG, "Unable to initialize Bluetooth")
                iWellNestData.onDeviceDisconnected()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.i(TAG, "onServiceDisconnected")
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.i(TAG, "Device connected")
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.i(TAG, "Device disconnected")
                iWellNestData.onDeviceDisconnected()
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                iWellNestData.onDeviceConnected()
                val data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA) ?: return
                val string = String(data)

                if (mBluetoothLeService?.isBluetoothLiveRecording == true && liveRecordingStarted) {
                    recordingData += data
                    Log.i(BluetoothLeService.TAG, "Data Count: ${recordingData.size} ${string} \n")
                    if (data.isNotEmpty()) {
                        iWellnestUsbData.addRawData(data)
                    }
                } else {
                    if (string.startsWith("+RECEND") || string.startsWith("RECEND")) {
                        mBluetoothLeService!!.sendMessage(ECGCMDHelper.instance.getDat(0, 5000, 1))
                    }
                    recordingData += data
                }

            } else if (BluetoothLeService.ACTION_CHARACTERISTIC_WRITTEN.equals(action)) {
                if (!hasPrepareRecording) {
                    hasPrepareRecording = true
                }
            }
        }
    }


    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter("android.bleutooth.device.action.UUID")
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
        intentFilter.addAction(BluetoothLeService.ACTION_CHARACTERISTIC_WRITTEN)

        return intentFilter
    }


    /*
    * start BLE Connection
    * */
    override fun startBleConnection() {
        val bluetoothDevice = SharedPreference.getBluetoothDevice(activity!!)
        if (bluetoothDevice != null) {
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val ecgWellnDevice = mBluetoothAdapter.getRemoteDevice(bluetoothDevice.address)
            if (mBluetoothAdapter.isEnabled && ecgWellnDevice != null) {
                mBluetoothAdapter.cancelDiscovery()
                connectBle(bluetoothDevice.address)
            } else {
                iWellNestData.onDeviceDisconnected()
            }
        } else {
            iWellNestData.onDeviceDisconnected()
        }
    }


    /* Disconnect BLE device */
    override fun disConnectBleService() {
        mBluetoothLeService?.let {
            with(mBluetoothLeService!!) {
                disconnect()
            }
        }
    }

    /*
    * sending cmd to ble device to connection*/
    override fun deviceConnect() {
        hasPrepareRecording = false
        mBluetoothLeService!!.sendMessage(
            ECGCMDHelper.instance.ecgTestCMD(),
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        )
    }

    /*
    * Prepare for start recording send cmd to ble device*/
    override fun prepareRecording() {
        hasPrepareRecording = true
        mRecordingStopped = false
        mBluetoothLeService!!.sendMessage(ECGCMDHelper.instance.ecgRecordCMD())

    }

    /*
   * register Broadcast receiver
   */
    override fun onResume() {
        try {
            activity?.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*
    * unregister Broadcast receiver
    */
    override fun onPause() {
        try {
            activity?.unregisterReceiver(mGattUpdateReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*
    * destroy service
    */
    override fun onDestroy() {
        activity?.let {
            with(activity!!) {
                if (m_serviceBound) {
                    unbindService(mServiceConnection)
                    m_serviceBound = false
                }
            }
        }
    }

    override fun setEcgSetup(setup: String) {
        this.iEcgSetup = setup
    }

    /*
    *
     */
    override fun displayGraphs() {
        val context = activity as Context
        val ecgView = EcgView(context)
        try {
            iWellNestGraph.setGraphView(
                ecgView.getGraphView(
                    iEcgSetup,
                    RecordingDataHandler.instance.recordingData!!
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun clearData() {
        RecordingDataHandler.instance.recordingData = null
    }

    /*
        Need to retrieve byte array in offline recordings. Use below function to get byte array

     */
    override fun obtainGraphByteArray(): ByteArray {
        val recordingBytesData = RecordingDataHelper.instance.getRecordingBytesData()
        var byteArray = ByteArray(0)

        for (index in 0 until recordingBytesData.size) {
            byteArray += recordingBytesData[index]
        }

        return byteArray
    }

    // upload file on AZURE
    // * @param random = randomId per file
    // * @param azureAccountName = provided by main module
    // * @param azureApplicationKey = provided by main module
    // * @param ecgRecordings = container Name of azure storage
    // *
    override fun uploadFile(
        azureHOST: String,
        random: String,
        sasToken: String,
        ecgRecordings: String,
        iECGRecordingUpload: IECGRecordingUpload
    ) {
        try {

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val recordingBytesData =
                        RecordingDataHelper.instance.getRecordingBytesData()
                    var byteArray = ByteArray(0)

                    for (index in 0..recordingBytesData.size - 1) {
                        byteArray += recordingBytesData[index]
                    }
                    //val  inputStream =  ByteArrayInputStream(byteArray)
                    val success =
                        uploadingFile(byteArray, random, ecgRecordings, sasToken, azureHOST)
                    if (success) {
                        iECGRecordingUpload.onSuccess()
                    } else {
                        iECGRecordingUpload.onFailure("There was some issue while upload the ecg.")
                    }
                } catch (e: Exception) {
                    iECGRecordingUpload.onFailure(e.message)
                }

            }
        } catch (e: Exception) {
            iECGRecordingUpload.onFailure(e.message)
        }
    }

    //file uploader via work manager
    override fun uploadFileViaWorkManager(
        byteArray: ByteArray,
        azureHOST: String,
        random: String,
        sasToken: String,
        ecgRecordings: String
    ): Boolean {
        var status: Boolean
        try {
            val success = uploadingFile(byteArray, random, ecgRecordings, sasToken, azureHOST)
            status = success
        } catch (e: Exception) {
            Log.e("UploadWorker", e.message)
            status = false
        }

        return status
    }

    @Throws(Exception::class)
    private fun getContainer(
        containerName: String,
        storageConnectionString: String
    ): CloudBlobContainer {
        // Retrieve storage account from connection-string.

        val storageAccount = CloudStorageAccount
            .parse(storageConnectionString)

        // Create the blob client.
        val blobClient = storageAccount.createCloudBlobClient()

        // Get a reference to a container.
        // The container name must be lower case

        return blobClient.getContainerReference(containerName)
    }


    @Throws(Exception::class)
    fun uploadingFile(
        source: ByteArray,
        uuid: String,
        containerName: String,
        sasToken: String,
        azureHOST: String
    ): Boolean {
        return try {
            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5,TimeUnit.MINUTES)
                .writeTimeout(5,TimeUnit.MINUTES)
                .build()

            val urlBuilder = HttpUrl.parse("$azureHOST$containerName/$uuid$sasToken")!!.newBuilder()

            val url = urlBuilder.build().toString()
            val body = RequestBody.create(MediaType.parse("text/plain"), source)

            val request = Request.Builder()
                .url(url)
                .addHeader("x-ms-blob-type", "BlockBlob")
                .addHeader("Content-Type", "text/plain")
                .method("PUT", body)
                .build()


            val response = client.newCall(request).execute()

            response.isSuccessful

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("File Upload", e.message)
            false
        }

    }

    override fun uploadPdfFile(
        source: File,
        uuid: String,
        containerName: String,
        sasToken: String,
        azureHOST: String
    ) {
        thread {
            try {
                val client = OkHttpClient()
                val urlBuilder =
                    HttpUrl.parse("$azureHOST$containerName/$uuid$sasToken")!!.newBuilder()

                val url = urlBuilder.build().toString()
                val body = RequestBody.create(MediaType.parse("text/plain"), source)

                val request = Request.Builder()
                    .url(url)
                    .addHeader("x-ms-blob-type", "BlockBlob")
                    .addHeader("Content-Type", "application/pdf")
                    .method("PUT", body)
                    .build()


                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.i(TAG, "Pdf $uuid Uploaded Successfully.")
                } else {
                    Log.e(TAG, "Pdf $uuid upload failed.!! Please check your connection.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("File Upload", e.message)
            }
        }
    }

    override fun getRecordingData(): ByteArray {
        return recordingData
    }

    /*
        Convert byte array to graph list (array list of array list (bytes))
     */

    override fun obtainGraphList(byteArray: ByteArray) {
        val handler = android.os.Handler(Looper.getMainLooper())

        handler.post {
            val finalRecordingData = ArrayList<List<Byte>>()
            var count = 0
            try {
                for (index in byteArray.indices) {
                    val reading =
                        byteArray.slice(IntRange(0 + count, count + 15))
                    finalRecordingData.add(reading)
                    count += 16
                }
            } catch (e: Exception) {
            }

            val chartData =
                recordBytes.setUpDataForRecording(finalRecordingData)
            val graphList = recordBytes.getGraphList(chartData, allData = true)
            iWellNestData.onRecordingCompleted(graphList)

            val context = activity as Context
            val ecgView = EcgView(context)
            iWellNestGraph.setGraphView(ecgView.getGraphView(iEcgSetup, graphList))
        }
    }

    override fun getFile(
        random: String,
        azureHOST: String,
        sasToken: String,
        ecgRecordings: String,
        iWellnestEcgFileListener: IWellnestEcgFileListener
    ) {
        try {
            val handler = android.os.Handler(Looper.getMainLooper())
            val thread = object : Thread() {
                override fun run() {
                    try {
                        try {
                            val storageCredentials =
                                StorageCredentialsSharedAccessSignature(sasToken)
                            val blockBlobReference = CloudBlockBlob(
                                URI.create("$azureHOST$ecgRecordings/$random"),
                                storageCredentials
                            )
                            blockBlobReference.downloadAttributes()
                            val imageBuffer =
                                ByteArray(blockBlobReference.properties.length.toInt())
                            if (imageBuffer.isNotEmpty()) {
                                blockBlobReference.downloadToByteArray(imageBuffer, 0)
                                handler.post {
                                    val finalRecordingData = ArrayList<List<Byte>>()

                                    var count = 0
                                    try {
                                        while(count <= imageBuffer.size-1) {
                                            val reading =
                                                imageBuffer.slice(IntRange(0 + count, count + 15))
                                            finalRecordingData.add(reading)
                                            count += 16
                                        }
                                        iWellnestEcgFileListener.onSuccess(finalRecordingData)
                                    } catch (e: Exception) {
                                        iWellnestEcgFileListener.onFailure(e)
                                    }


//                                     val chartData =
//                                        recordBytes.setUpDataForRecording(finalRecordingData)
//
//                                        iWellNestData.onRecordingCompleted(chartData)
//                                        val graphList = recordBytes.getGraphList(chartData, bpm,allData = allData)
//                                        val context = activity as Context
//                                        val ecgView = EcgView(context)
//                                        iWellNestGraph.setGraphView(
//                                            ecgView.getGraphView(
//                                                iEcgSetup,
//                                                graphList
//                                            )
//                                        )
//                                    } else {
//                                        val graphList = recordBytes.getGraphList(chartData, bpm)
//                                        iWellNestData.onRecordingCompleted(graphList)
//                                        val context = activity as Context
//                                        val ecgView = EcgView(context)
//                                        iWellNestGraph.setGraphView(
//                                            ecgView.getGraphView(
//                                                iEcgSetup,
//                                                graphList
//                                            )
//                                        )
//                                    }
                                }
                            } else {
                                handler.post {
                                    iWellnestEcgFileListener.onFailure(FileNotFoundException("no data available"))
                                }
                            }
                        } catch (e: Exception) {
                            handler.post {
                                iWellnestEcgFileListener.onFailure(e)
                            }
                        }

                    } catch (e: Exception) {
                        handler.post {
                            iWellnestEcgFileListener.onFailure(e)
                        }
                    }

                }
            }
            thread.start()
        } catch (e: Exception) {
            iWellnestEcgFileListener.onFailure(e)
        }
    }

    override fun setUpUsbConnection(device: UsbDevice) {

        val usbManager = (activity as Context).getSystemService(Context.USB_SERVICE) as UsbManager
        val usbConnection = usbManager.openDevice(device)
        mUsbSerialDevice = UsbSerialDevice.createUsbSerialDevice(device, usbConnection)

        mUsbSerialDevice?.open()
        mUsbSerialDevice?.setBaudRate(230400)
        mUsbSerialDevice?.setDataBits(UsbSerialInterface.DATA_BITS_8)
        mUsbSerialDevice?.setParity(UsbSerialInterface.PARITY_NONE)
        mUsbSerialDevice?.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)

        setUpUsbCallback()
//        usbReadThread = ReadThread(this)
//        usbReadThread?.start()

    }

    override fun sendTstEndUsb() {
        mUsbSerialDevice?.write(UsbCommandHelper.sendCmd())
        Thread.sleep(250)

        mUsbSerialDevice?.write(UsbCommandHelper.sendCmd())
        Thread.sleep(250)


        mUsbSerialDevice?.write(UsbCommandHelper.sendCmd())
        Thread.sleep(250)

    }

    override fun setUsbDataDelegate(iWellnestUsbData: IWellnestUsbData) {
        this.iWellNestData = iWellNestData
    }

    private class ReadThread(val wellNestUtil: WellNestUtil) : Thread() {
        override fun run() {
            while (true) {
                val buffer = ByteArray(100)
                val n: Int = wellNestUtil.mUsbSerialDevice?.syncRead(buffer, 0) ?: 0
                if (n > 0) {
                    wellNestUtil.processUsbData(buffer)
                }
            }
        }
    }

    override fun setUpUsbCallback() {
        val usbCallback = UsbSerialInterface.UsbReadCallback { data ->
            processUsbData(data)
        }

        this.mUsbSerialDevice?.read(usbCallback)
    }

    private fun processUsbData(data: ByteArray) {
        if (data.isNotEmpty()) {
            val string = String(data)

            Log.i(TAG, data.toHex())

            if (string.startsWith("+GETDID")) {
                val id = string.split("=")[1]
                iWellnestUsbData.setDidUsb(id)
            } else if (string.startsWith("+GETSTA")) {
                val batteryLevel = string.split("=")[1].split(",")[0]
                iWellnestUsbData.setBatteryStatus(batteryLevel)
            } else {

                if (string.startsWith("+RECEND")) {
                    CoroutineScope(Dispatchers.Default).launch {
                        mUsbSerialDevice?.write(UsbCommandHelper.instance.setSampleCommand(500))
                        delay(250)
                        mUsbSerialDevice?.write(
                            ECGCMDHelper.instance.getDat(0, 5000, 1).toByteArray()
                        )
                    }
                } else {
                    synchronized(recordingData) {
                        recordingData += data
                        //                        CoroutineScope(Dispatchers.Main).launch {
                        //                            // delay(100)
                        //                            val resultantArray = liveBytes.getLiveRecordedData(data)
                        //                            iWellnestUsbData.plotGraphPoint(resultantArray)
                        //                        }
                        //
                        //
                        //                        //minimum 20 second recording essential for display best graphs.
                        //                        RecordingDataHandler.instance.recordingData = null
//for usb
                        iWellnestUsbData.addRawData(data)
                    }

                }
            }
        }
    }

    override fun startUsbRecording(isUsbRecording: Boolean) {

        if (isUsbRecording) {
//            mUsbSerialDevice!!.write(UsbCommandHelper.instance.sendStreamCmd())
//            Thread.sleep(250)
//            mUsbSerialDevice!!.write(UsbCommandHelper.instance.sendStartCmd())
//            Thread.sleep(250)
//            mUsbSerialDevice!!.write(UsbCommandHelper.instance.sendStatusCmd())
//            Thread.sleep(250)
//            mUsbSerialDevice!!.write(UsbCommandHelper.instance.authorizeCmd())
//            Thread.sleep(250)
//            mUsbSerialDevice!!.write(UsbCommandHelper.instance.sendCmd())
//            Thread.sleep(250)
            mUsbSerialDevice?.write(UsbCommandHelper.instance.startStreamCmd())
        } else {
            mUsbSerialDevice?.write(UsbCommandHelper.instance.sendCmd())
            Thread.sleep(250)
            mUsbSerialDevice?.write(ECGCMDHelper.instance.ecgRecordCMD().toByteArray())
        }

    }

    override fun stopUsbRecording(processRecording: Boolean) {
        mUsbSerialDevice?.write(UsbCommandHelper.instance.stopStreamCmd())
//        usbReadThread?.join()
//        usbReadThread?.interrupt()
        if (processRecording) {
            RecordingDataHelper.instance.clearChartData()
            val recordingData = recordBytes.setRecordingData(recordingData)
            val graphList = recordBytes.getGraphList(recordingData)
            iWellNestData.onRecordingCompleted(graphList)
            RecordingDataHandler.instance.recordingData = graphList
        }
    }

    override fun getBatteryStatusUsb() {
        Thread.sleep(200)
        mUsbSerialDevice?.write(UsbCommandHelper.instance.getBatteryStatus())

        Thread.sleep(200)
        mUsbSerialDevice?.write(UsbCommandHelper.instance.getBatteryStatus())

        Thread.sleep(200)
        mUsbSerialDevice?.write(UsbCommandHelper.instance.getBatteryStatus())

    }

    override fun getDidUsb() {
        CoroutineScope(Dispatchers.Default).launch {
            delay(200)
            mUsbSerialDevice?.write(UsbCommandHelper.instance.getDid())
        }
    }

    override fun startBluetoothLiveRecording() {
        if (!liveRecordingStarted) {
            mBluetoothLeService?.sendMessage("+GETSTR=1\r\n")
            liveRecordingStarted = true
        }
    }

    override fun stopBluetoothLiveRecording(processRecording: Boolean) {
        mBluetoothLeService?.sendMessage("+GETSTR=0\r\n")
        liveRecordingStarted = false
        if (processRecording) {
            RecordingDataHelper.instance.clearChartData()
            val parsedData = recordBytes.setRecordingData(recordingData)
            val graphList = recordBytes.getGraphList(parsedData, allData = true)
            iWellNestData.onRecordingCompleted(parsedData)

            RecordingDataHandler.instance.recordingData = graphList
        }

    }

    override fun stopProcessing() {
        mRecordingStopped = true
        RecordingDataHelper.instance.clearChartData()
        val parsedData = recordBytes.setRecordingData(recordingData)
        val graphList = recordBytes.getGraphList(parsedData, allData = true)
        iWellNestData.onRecordingCompleted(graphList)

        RecordingDataHandler.instance.recordingData = graphList

        val context = activity as Context
        val ecgView = EcgView(context)
        iWellNestGraph.setGraphView(ecgView.getGraphView(iEcgSetup, graphList))
        mRecordingStopped = false
    }
}

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
