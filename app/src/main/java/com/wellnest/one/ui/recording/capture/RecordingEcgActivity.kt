package com.wellnest.one.ui.recording.capture

import android.bluetooth.BluetoothGattCharacteristic
import android.content.*
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.rc.wellnestmodule.BluetoothLeService
import com.rc.wellnestmodule.interfaces.ISendMessageToEcgDevice
import com.wellnest.one.R
import com.wellnest.one.data.local.user_pref.PreferenceManager
import com.wellnest.one.databinding.ActivityRecordEcgBinding
import com.wellnest.one.model.Symptoms
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.recording.calibration.EcgCalibrationActivity
import com.wellnest.one.utils.DialogHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Hussain on 16/11/22.
 */
@AndroidEntryPoint
class RecordingEcgActivity : BaseActivity(), ISendMessageToEcgDevice, View.OnClickListener {

    private lateinit var binding : ActivityRecordEcgBinding

    private var bluetoothLeService: BluetoothLeService? = null

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            statusBarColor =
                ContextCompat.getColor(this@RecordingEcgActivity, R.color.activity_bkg)// S
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_record_ecg)
        binding.btnRecording.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)

        val reRecord = intent.getBooleanExtra("reRecord", false)
        if (reRecord) {
            binding.btnRecording.text = "Re-Record ECG"
            setBluetoothState()
        }

        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(
            gattServiceIntent,
            mServiceConnection,
            Context.BIND_AUTO_CREATE
        )

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (bluetoothLeService == null) {
                    return
                }
                if (bluetoothLeService!!.bleWriteCharacteristics == null) {
                    return
                }
                CoroutineScope(Dispatchers.Main).launch {
                    bluetoothLeService!!.sendingMessage(
                        bluetoothLeService!!.bleWriteCharacteristics!!,
                        "+ELESTA\r\n"
                    )
                    bluetoothLeService!!.sendingMessage(
                        bluetoothLeService!!.bleWriteCharacteristics!!,
                        "+GETSTA\r\n"
                    )
                }
            }
        }, 600)
    }

    override fun onResume() {
        super.onResume()
        try {
            registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
            setBluetoothState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unbindService(mServiceConnection)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnRecording -> {
                val ecgCalibration = Intent(this,EcgCalibrationActivity::class.java)
                intent.extras?.let { ecgCalibration.putExtras(it) }
                startActivity(ecgCalibration)
            }

            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }

    private fun setBluetoothState() {

        if (bluetoothLeService?.isConnected() == false) {
//            val eventProp = JSONObject()
//            eventProp.put("ecgDeviceId", bluetoothDevice?.deviceId)

            //eventProp.put("patientId", patient.id)
//            Amplitude.getInstance().logEvent("Start Pairing (New Recording)", eventProp)
            binding.btnRecording.tag = false

        } else {
            binding.btnRecording.tag = true
            binding.imgStatus.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.gradient_bkg
                )
            )
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


    private fun disconnected() {
        //binding.llRecording.tag = false
        binding.btnRecording.tag = false
        binding.imgStatus.setImageResource(R.drawable.red_gradient_bkg)
    }

    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            bluetoothLeService = (service as BluetoothLeService.LocalBinder).service
            bluetoothLeService!!.setSendingMessageCallbacks(this@RecordingEcgActivity)
            if (!bluetoothLeService!!.initialize()) {
//                Log.i(DeviceConnectionActivity.TAG, "Unable to initialize Bluetooth")
                //  finish()
            }

        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothLeService = null
            //WellNestLoader.dismissLoader()
        }
    }


    private val gattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
//                deviceDisconnected()
                disconnected()
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                val supportedGattServices = bluetoothLeService!!.supportedGattServices
                if (supportedGattServices != null && supportedGattServices.size > 0) {
                    val characteristics =
                        supportedGattServices.get(supportedGattServices.size - 1).characteristics
                    for (characteristic in characteristics) {
                        if (characteristic.properties == BluetoothGattCharacteristic.PROPERTY_NOTIFY) {
//                            bluetoothLeService!!.setCharacteristicNotification(characteristic, true)
                        }
                    }
                }

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                try {

                    val byteArrayExtra = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA)
                    byteArrayExtra?.let {
                        val string = String(it)
                        Log.d("RecordingECG", "onReceive: ${string}")
                        if (string.startsWith("+ELESTA") || string.startsWith("ELESTA")) {
//                            try {
//                                val bits = string.split("=")[1].replace("\r\n", "")
//                                val hexStringToByteArray = ConvertHelper.hexStringToByteArray(bits)
//                                hexStringToByteArray?.let { _ ->
//                                    updateElectrodeStatus()
//                                }
//                            } catch (e: java.lang.Exception) {
//                                e.printStackTrace()
//                            }

                        }
                        if (string.startsWith("+GETSTA") || string.startsWith("GETSTA")) {
                            val batteryLevel = string.split("=")[1].split(",")[0]
                            if (batteryLevel.equals("0")) {
                                // level is low fill the progress 10% with red color
                                binding.progressBar.progress = 10

                                Handler().post {
                                    val progressDrawable: Drawable =
                                        binding.progressBar.getProgressDrawable().mutate()
                                    progressDrawable.setColorFilter(
                                        getResources().getColor(R.color.red),
                                        PorterDuff.Mode.SRC_IN
                                    )
                                    binding.progressBar.setProgressDrawable(progressDrawable)
                                }

                                DialogHelper.showDialog(
                                    "Low Battery",
                                    "Please charge your ECG device for accurate results.",
                                    this@RecordingEcgActivity
                                ) { dialog, _ ->
//                                    WellNestProUtil.pushLandingActivity(this@RecordEcgActivity)
                                    dialog.dismiss()
                                }

                            } else if (batteryLevel.equals("1")) {
                                // level is 25%  fill the progress 25% with yellow color
                                binding.progressBar.progress = 25

                                Handler().post {
                                    val progressDrawable: Drawable =
                                        binding.progressBar.getProgressDrawable().mutate()
                                    progressDrawable.setColorFilter(
                                        getResources().getColor(R.color.condition_color),
                                        PorterDuff.Mode.SRC_IN
                                    )
                                    binding.progressBar.setProgressDrawable(progressDrawable)
                                }

//                                binding.btnRecording.isEnabled = true

                            } else if (batteryLevel.equals("2")) {
                                // level is 50%  fill the progress 50% with half color
                                binding.progressBar.progress = 75

                                Handler().post {
                                    val progressDrawable: Drawable =
                                        binding.progressBar.getProgressDrawable().mutate()
                                    progressDrawable.setColorFilter(
                                        getResources().getColor(R.color.half),
                                        PorterDuff.Mode.SRC_IN
                                    )
                                    binding.progressBar.setProgressDrawable(progressDrawable)
                                }
//                                binding.btnRecording.isEnabled = true

                            } else if (batteryLevel.toInt() >= 3) {
                                // level is 100%  fill the progress 100% with green color
                                binding.progressBar.progress = 100

                                Handler().post {
                                    val progressDrawable: Drawable =
                                        binding.progressBar.getProgressDrawable().mutate()
                                    progressDrawable.setColorFilter(
                                        getResources().getColor(R.color.green),
                                        PorterDuff.Mode.SRC_IN
                                    )
                                    binding.progressBar.setProgressDrawable(progressDrawable)
                                }
//                                binding.btnRecording.isEnabled = true

                            }

                            Log.e("Battery", "Battery Status Set")
                        }
                        if (string.startsWith("+SHUTDOWN")) {
                            deviceDisconnected()
                            disconnected()
                        }
                    }

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } else if (BluetoothLeService.ACTION_CHARACTERISTIC_WRITTEN.equals(action)) {

                // bluetoothLeService!!.sendMessage(ECGCMDHelper.instance.ecgTestCMD())
            }
        }
    }

    override fun sendMessage(characteristic: BluetoothGattCharacteristic, i: Int) {
    }

}