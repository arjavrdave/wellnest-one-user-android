package com.wellnest.one.ui.recording.calibration

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.rc.wellnestmodule.BluetoothLeService
import com.rc.wellnestmodule.interfaces.IRecordData
import com.rc.wellnestmodule.interfaces.ISendMessageToEcgDevice
import com.rc.wellnestmodule.interfaces.IWellnestGraph
import com.rc.wellnestmodule.interfaces.IWellnestUsbData
import com.rc.wellnestmodule.utils.RecordBytesFactory
import com.wellnest.one.R
import com.wellnest.one.databinding.ActivityEcgCalibrationBinding
import com.wellnest.one.model.SetupMessageInfo
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.recording.capture.BluetoothLiveEcgActivity
import com.wellnest.one.utils.DialogHelper
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.Runnable
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Created by Hussain on 16/11/22.
 */
class EcgCalibrationActivity : BaseActivity(), ISendMessageToEcgDevice, IWellnestGraph,
    IWellnestUsbData {

    private var adapter: SetupMessageAdapter = SetupMessageAdapter {
        navigateNext()
    }
    private var bluetoothLeService: BluetoothLeService? = null
    private lateinit var binding: ActivityEcgCalibrationBinding

    private var rawData = ConcurrentLinkedQueue<Byte>()

    private val scope = CoroutineScope(Dispatchers.IO)

    private val recordByteHandler: IRecordData by lazy {
        RecordBytesFactory().getRecordBytes()
    }


    private var isCalibrated = false

    private var graphCalibrated = ArrayList<Boolean>(12)
    private var graphAvg = ArrayList<Double>(12)

    private val TAG = "EcgCalibrationActivity"
    private val remoteConfig = FirebaseRemoteConfig.getInstance()
    private var threshold = -0.8

    private val handler = Handler(Looper.getMainLooper())
    private var setupMessageInfo: SetupMessageInfo? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val params =
            JSONObject("{\"active\":true,\"messages\":[\"Processing your request\",\"Checking your device connection\",\"Securing network\",\"Checking electrode connections\",\"Fetching data\",\"Calibrating the device\",\"Encrypting data\",\"Getting your device ready\"],\"msgTime\":2000,\"successTime\":300,\"errorMsg\":\"Please verify your connections!\",\"threshold\":-0.8}")
        val defaults = HashMap<String, String>()
        defaults["ECGChecklist"] = params.toString()

        remoteConfig.setDefaultsAsync(defaults as Map<String, Any>)

        remoteConfig
            .fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "${task.result}")
                    setupMessages(remoteConfig.getString("ECGChecklist"))
                } else {
                    DialogHelper.showDialog(
                        "Error",
                        task.exception?.localizedMessage ?: "Something Went Wrong",
                        this
                    ) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    Log.i(TAG, "${task.exception?.localizedMessage}")
                }
            }
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            statusBarColor =
                ContextCompat.getColor(this@EcgCalibrationActivity, R.color.white)// S
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ecg_calibration)

        wellNestUtil.startBleService(this, this, this, this, this)

        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(
            gattServiceIntent,
            mServiceConnection,
            Context.BIND_AUTO_CREATE
        )


        binding.imgBack.setOnClickListener {
            finish()
        }

    }

    private fun setupMessages(ecgList: String) {
        val jsonObject = JSONObject(ecgList)
        val msgTime = if (jsonObject.has("msgTime")) jsonObject.getInt("msgTime") else 2000
        val successTime =
            if (jsonObject.has("successTime")) jsonObject.getInt("successTime") else 200
        val threshold = if (jsonObject.has("threshold")) jsonObject.getDouble("threshold") else -0.8
        val errorMsg =
            if (jsonObject.has("errorMsg")) jsonObject.getString("errorMsg") else "Please verify your connections!"
        val msg = mutableListOf<String>()
        if (jsonObject.has("messages")) {
            val messages = jsonObject.getJSONArray("messages")
            Log.i(TAG, messages.toString())
            for (i in 0 until messages.length()) {
                msg.add(messages.getString(i))
            }
        } else {
            msg.add("Processing your request")
            msg.add("Checking your device connection")
            msg.add("Securing network")
            msg.add("Checking electrode connections")
            msg.add("Fetching data")
            msg.add("Calibrating the device")
            msg.add("Encrypting data")
            msg.add("Getting your device ready")
        }
        setupMessageInfo = SetupMessageInfo(
            msg,
            msgTime,
            successTime,
            totalTime = msgTime * msg.size,
            errorMsg,
            threshold
        )
        adapter.setupMessage(setupMessageInfo!!)
        binding.rvMessages.adapter = adapter
        handler.postDelayed(cancelRunnable, setupMessageInfo!!.totalTime.toLong())

        adapter.setupMessage(setupMessageInfo!!)
        binding.rvMessages.adapter = adapter
        this.threshold = setupMessageInfo!!.threshold
    }

    private val cancelRunnable = Runnable {
        wellNestUtil.stopBluetoothLiveRecording(false)
        // show error
        DialogHelper.showDialog("Error", setupMessageInfo?.errorMsg, this) { dialog, _ ->
            dialog.dismiss()
            finish()
        }
    }

    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            bluetoothLeService = (service as BluetoothLeService.LocalBinder).service
            bluetoothLeService!!.setSendingMessageCallbacks(this@EcgCalibrationActivity)
            if (!bluetoothLeService!!.initialize()) {
                //  finish()
            }

        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothLeService = null
            //WellNestLoader.dismissLoader()
        }
    }


    override fun onResume() {
        super.onResume()
        startParsingData()
        handler.postDelayed({
            wellNestUtil.startBluetoothLiveRecording()
        }, 50)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(cancelRunnable)
    }

    private fun startParsingData() {
        scope.launch(Dispatchers.IO) {
            for (i in 0 until 12) {
                graphCalibrated.add(false)
                graphAvg.add(0.0)
            }

            var n = 1
            while (true) {
                while (rawData.isEmpty() && rawData.peek()?.toInt() != -70) {
                    rawData.poll()
                }


                if (rawData.size > 15) {
                    val data = mutableListOf<Byte>()
                    synchronized(rawData) {
                        for (i in 0..15) {
                            data.add(rawData.poll() ?: 0x0)
                        }
                    }

                    val finalRecordingData = ArrayList<List<Byte>>()

                    finalRecordingData.add(data)
                    val parsedData = recordByteHandler.setUpDataForRecording(finalRecordingData)

                    // calculate average
                    for (i in 0 until 12) {
                        graphAvg[i] = (((n - 1).toDouble()) * graphAvg[i] + parsedData[0][i]) / n
                    }

                    val allCalibrated = graphAvg.filter { avg -> avg > threshold }.size == 12

                    n += 1

                    if (allCalibrated) {
                        graphAvg.forEach {
                            Log.i(TAG,"$it}")
                        }
                        wellNestUtil.stopBluetoothLiveRecording(false)
                        isCalibrated = true
                        withContext(Dispatchers.Main) {
                            binding.rvMessages.forEachVisibleHolder<SetupMessageAdapter.SetupMessageVH> { holder ->
                                holder.setupSuccessTimer()
                            }
                        }
                        break
                    }
                }
            }
        }
    }

    private fun navigateNext() = runOnUiThread {
        val bleLiveIntent = Intent(this, BluetoothLiveEcgActivity::class.java)
        intent.extras?.let { bleLiveIntent.putExtras(it) }
        startActivity(bleLiveIntent)
        finish()
    }


    override fun addRawData(data: ByteArray) {
        rawData.addAll(data.toList())
    }
}