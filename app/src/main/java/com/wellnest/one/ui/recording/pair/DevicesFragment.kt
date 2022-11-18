package com.wellnest.one.ui.recording.pair

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rc.wellnestmodule.BluetoothLeService
import com.rc.wellnestmodule.interfaces.IBluetooth
import com.rc.wellnestmodule.interfaces.ISendMessageToEcgDevice
import com.rc.wellnestmodule.models.ECGDevice
import com.rc.wellnestmodule.utils.ECGCMDHelper
import com.wellnest.one.R
import com.wellnest.one.data.local.user_pref.PreferenceManager
import com.wellnest.one.databinding.FragmentDevicesBinding
import com.wellnest.one.model.ECGPrivateKey
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.home.HomeActivity
import com.wellnest.one.utils.DialogHelper
import com.wellnest.one.utils.ProgressHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
open class DevicesFragment : Fragment(), IBluetooth, ISendMessageToEcgDevice {

    companion object {
        val TAG = toString()
    }

    private var deviceId: String = ""
    private lateinit var binding: FragmentDevicesBinding
    private lateinit var scanSettings: ScanSettings
    private lateinit var scanFilter: ScanFilter
    val deviceList = ArrayList<BluetoothDevice>()
    private var mHandler: Handler = Handler()
    private var mScanning: Boolean = false
    var scanFilterList = ArrayList<ScanFilter>()
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var deviceAdapter: BluetoothDeviceAdapter
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothLeService: BluetoothLeService? = null
    val randomUUID = UUID.randomUUID().toString().replace("-", "")
    private var response: String = ""
    private var auth: String = ""
    private var mEcgDeviceId = -1

    private val deviceViewModel: DeviceViewModel by viewModels()

    private var authenticationComplete = false

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_devices, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        deviceAdapter = BluetoothDeviceAdapter(requireActivity(), this, deviceList)
        binding.recyclerView.adapter = deviceAdapter


        deviceViewModel.error.observe(requireActivity()) {
            it?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        deviceViewModel.privateKey.observe(requireActivity()) {
            it?.let { privKey ->
                preferenceManager.saveKey(deviceId, privKey)
                processPrivateKey(privKey)
            }
        }
    }

    override fun onStop() {
        super.onStop()

        if (!authenticationComplete) {
            bluetoothLeService?.disconnect()
        }
    }

    private fun processPrivateKey(it: ECGPrivateKey) {
        it.let {
            val privateKey = it.privateKey
            mEcgDeviceId = it.id
            if (auth.isEmpty()) {
                return
            }
            val decryptedByteArray = decryptByteArray(
                hexStringToByteArray(auth.split("=")[1].split(":")[0])!!,
                privateKey
            )

            if (randomUUID.toUpperCase().equals(decryptedByteArray)) {
                deviceConnected()
                val encryptedByteArray = encryptByteArray(
                    hexStringToByteArray(auth.split("=")[1].split(":")[1])!!,
                    privateKey
                )
                Log.i("CMDS", "+AUTHOR=${encryptedByteArray}\n")
                bluetoothLeService!!.sendMessage("+AUTHOR=${encryptedByteArray}\r\n")
                authenticationComplete = true
            } else {
            }
        }
    }

    private fun scanLeDevice(enable: Boolean) {
        when (enable) {
            true -> {
                // Stops scanning after a pre-defined scan period.
//                mHandler.postDelayed({
//                    scanLeDevice(false)
//                }, SCAN_PERIOD)
                mScanning = true
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    bluetoothLeScanner!!.startScan(scanFilterList, scanSettings, scanCallback)
                }
            }
            else -> {
                mScanning = false
                if (bluetoothLeScanner != null && bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    bluetoothLeScanner!!.stopScan(scanCallback)
                }
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if (result != null) {
//                if (!hasBleDetected){
                // binding.tvHint.visibility = View.GONE
                bluetoothAdapter.cancelDiscovery()
                if (result.device.name != null && result.device.name.toLowerCase()
                        .startsWith("wellnest") && result.device.name.toLowerCase().contains("v4.0")
                ) {
                    deviceList.add(result.device)
                    var bluetoothDevices: Set<BluetoothDevice> = HashSet(deviceList)
                    val deviceList = ArrayList<BluetoothDevice>(bluetoothDevices)
                    deviceAdapter.addBluetoothDevices(deviceList)
                }

//                }

            }
            super.onScanResult(callbackType, result)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
        }
    }

    private val mReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        ?: return
                Log.i(TAG, device.name + "\n" + device.address)

            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.v(" ", "Connected ")
                Toast.makeText(requireContext(), "ACL Connected", Toast.LENGTH_SHORT).show()
                // requireContext().unregisterReceiver(this)

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.v(TAG, "disconnected ")
                Toast.makeText(requireContext(), "ACL Disconnected", Toast.LENGTH_SHORT).show()

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(requireContext(), "Discovery Finished", Toast.LENGTH_SHORT).show()
                requireContext().unregisterReceiver(this)
            }


        }
    }

    @SuppressLint("MissingPermission")
    override fun connect(bluetoothDevice: BluetoothDevice) {
        scanLeDevice(false)
        ProgressHelper.showDialog(requireActivity())
        if (bluetoothLeService?.isConnected() == true) bluetoothLeService?.disconnect()
        if (bluetoothDevice.name != null) {
            bluetoothLeService?.isV4 = !bluetoothDevice.name.toLowerCase().contains("v3")
            bluetoothLeService?.isBluetoothLiveRecording =
                !bluetoothDevice.name.toLowerCase().contains("v3")
        }



        bluetoothLeService!!.connect(bluetoothDevice.address)
        this.bluetoothDevice = bluetoothDevice


        CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            ProgressHelper.dismissDialog()
        }
    }

    override fun disConnect() {

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

    override fun onResume() {
        super.onResume()
        try {
            requireContext().registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            requireContext().unregisterReceiver(gattUpdateReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private val gattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothLeService.ACTION_GATT_CONNECTED == action) {
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED == action) {
                //WellNestLoader.dismissLoader()
                Toast.makeText(requireContext(), "Bluetooth disconnected", Toast.LENGTH_SHORT)
                    .show()
                response = ""
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == action) {
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                try {
                    val byteArrayExtra = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA)
                    if (byteArrayExtra != null) {
                        processCommand(byteArrayExtra)
                    }
//                    }

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } else if (BluetoothLeService.ACTION_CHARACTERISTIC_WRITTEN.equals(action)) {

                //bluetoothLeService!!.sendMessage(ECGCMDHelper.instance.ecgTestCMD())
            }
        }
    }


    override fun sendMessage(characteristic: BluetoothGattCharacteristic, i: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            bluetoothLeService!!.sendMessage(
                ECGCMDHelper.instance.ecgTestCMD(),
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            )
            delay(1000)
            bluetoothLeService!!.sendMessage(ECGCMDHelper.instance.ecgGETDIDCMD())
        }
    }

    private fun deviceConnected() {
        ProgressHelper.dismissDialog()

        try {
            preferenceManager.saveBleDevice(
                ECGDevice(
                    bluetoothDevice.address,
                    bluetoothDevice.name,
                    mEcgDeviceId
                )
            )

            DialogHelper.showDialog(
                "",
                "Device Connected Successfully",
                requireContext()
            ) { dialog, which ->
                dialog?.dismiss()
                val prepareRecording = arguments?.getBoolean("prepareRecording", false)
                val isHomePage = arguments?.getBoolean("isHomePage", false)
                val atLogin = arguments?.getBoolean("atLogin", false)

                if (atLogin != null && atLogin) {
                    val bluetoothDevice =
                        preferenceManager.getBluetoothDevice()


                    val eventProp = JSONObject()

                    bluetoothDevice.let {
                        if (it != null) {
                            eventProp.put("ecgDeviceId", it.deviceId)
                        }
                    }

//                    Amplitude.getInstance()
//                        .logEvent("ECGDevice Connected (Login)", eventProp)
                }


                if (prepareRecording != null && prepareRecording) {

                    val bluetoothDevice =
                        preferenceManager.getBluetoothDevice()

                    val eventProp = JSONObject()

                    bluetoothDevice.let {
                        if (it != null) {
                            eventProp.put("ecgDeviceId", it.deviceId)
                        }
                    }


                    if (isHomePage!!) {
                        requireActivity().startActivity(
                            Intent(
                                requireContext(),
                                SymptomsActivity::class.java
                            )
                        )
                        requireActivity().finish()
                    } else {
                        requireActivity().setResult(Activity.RESULT_OK)
                        requireActivity().finish()
                    }

                } else {
                    val homeIntent = Intent(requireActivity(), HomeActivity::class.java)
                    homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    requireActivity().startActivity(homeIntent)
                    requireActivity().finish()
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            DialogHelper.showDialog(
                "Error!",
                "There was some issue while pairing, please try again!",
                requireContext()
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            requireActivity().unbindService(mServiceConnection)
            requireContext().unregisterReceiver(mReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startBluetoothDiscovery() {
        try {
            //   this.bluetoothLeService = bluetoothLeService
            //check here for device supoort the BLE
            if (!requireActivity().getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
            ) {
                Toast.makeText(
                    requireContext(),
                    "Your device does not support the Bluetooth features",
                    Toast.LENGTH_SHORT
                ).show()
                requireActivity().finish()
            }
            val gattServiceIntent = Intent(requireContext(), BluetoothLeService::class.java)
            requireActivity().bindService(
                gattServiceIntent,
                mServiceConnection,
                Context.BIND_AUTO_CREATE
            )

            mHandler = Handler()

            bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

            scanFilter = ScanFilter.Builder()
//                .setServiceUuid(ParcelUuid.fromString(serviceUUID))
                .build()

            scanFilterList.add(scanFilter)

            scanSettings = ScanSettings.Builder()
                .build()




            bluetoothAdapter.startDiscovery()

            val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            filter.addAction(BluetoothDevice.ACTION_FOUND)
            requireContext().registerReceiver(mReceiver, filter)

            scanLeDevice(true)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    // Code to manage Service lifecycle.
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            bluetoothLeService = (service as BluetoothLeService.LocalBinder).service
            bluetoothLeService!!.setSendingMessageCallbacks(this@DevicesFragment)
            if (!bluetoothLeService!!.initialize()) {
                //  finish()
            }

        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothLeService = null
            //WellNestLoader.dismissLoader()
        }
    }

    private fun encryptByteArray(array: ByteArray, privateKey: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/ECB/NoPadding")
            val key = getPrivateKey(privateKey)
            val secretKey = SecretKeySpec(key, "AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val doFinal = cipher.doFinal(array)
            byteArrayToHexString(doFinal)!!
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun decryptByteArray(data: ByteArray, privateKey: String): String {
        try {
            val cipher = Cipher.getInstance("AES/ECB/NoPadding")
            return if (data.size <= cipher.blockSize) {
                val key = getPrivateKey(privateKey)
                val secretKey = SecretKeySpec(key, "AES")
                cipher.init(Cipher.DECRYPT_MODE, secretKey)
                val doFinal = cipher.doFinal(data)
                byteArrayToHexString(doFinal)!!
            } else {
                ""
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    private fun getPrivateKey(privateKey: String?): ByteArray {
        return privateKey!!.toByteArray()
    }

    private fun hexStringToByteArray(s: String): ByteArray? {
        var data: ByteArray? = null
        try {
            val len = s.length
            data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                        + Character.digit(s[i + 1], 16)).toByte()
                i += 2
            }
        } catch (e: Exception) {
            Log.e(TAG, "hexStringToByteArray: $e.message")
        }

        return data
    }

    private val hexArray =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    private fun byteArrayToHexString(bytes: ByteArray): String? {
        val hexChars = CharArray(bytes.size * 2)
        var v: Int
        for (j in bytes.indices) {
            try {
                v = bytes[j].toInt()
                hexChars[j * 2] = hexArray[v and 0xff ushr 4]
                hexChars[j * 2 + 1] = hexArray[v and 0x0F]
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return String(hexChars)
    }

    private fun processCommand(byteArrayExtra: ByteArray) {
        val string = String(byteArrayExtra)
        if (string.contains("\r\n")) {
            response += string.replace("\r\n", "")
            Log.v(BluetoothLeService.TAG, "Response Value : $response")
            if (response.startsWith("+GETDID")) {
                deviceId = response.split("=")[1]
                Log.d(TAG, "onReceive: ${response}")
                response = ""
                Log.i("CMD", "+GETAUT=${randomUUID}\n")
                bluetoothLeService!!.sendMessage("+GETAUT=${randomUUID}\r\n")
            }
            if (response.startsWith("+GETAUT")) {
                auth = response
                Log.d(TAG, "onReceive: ${response}")
                try {
                    if (deviceId.length > 0) {

                        // check if private key exist locally
                        val privateKey = preferenceManager.getPrivateKey(
                            deviceId
                        )
                        if (privateKey != null) {
                            processPrivateKey(privateKey)
                        } else {
                            val isInternetConnected =
                                (requireActivity() as BaseActivity).isConnectedToInternet(
                                    requireContext()
                                )
                            if (isInternetConnected) {
                                response = ""
                                deviceViewModel.getPrivateKey(deviceId)
                            } else {

                                DialogHelper.showDialog(
                                    "Error!!",
                                    getString(R.string.privatekey_error),
                                    requireContext()
                                ) { dialog, _ ->
                                    bluetoothLeService?.disconnect()
                                    dialog.dismiss()
                                }

                            }
                        }
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            response = ""
        } else {
            if (response != string && !response.contains(string, true))
                response += string
            Log.v(BluetoothLeService.TAG, "Response Value : $response")
        }

    }
}
