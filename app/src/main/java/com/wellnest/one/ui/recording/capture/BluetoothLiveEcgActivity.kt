package com.wellnest.one.ui.recording.capture

import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.rc.wellnestmodule.interfaces.IRecordData
import com.rc.wellnestmodule.interfaces.ISendMessageToEcgDevice
import com.rc.wellnestmodule.interfaces.IWellnestGraph
import com.rc.wellnestmodule.interfaces.IWellnestUsbData
import com.rc.wellnestmodule.utils.RecordBytesFactory
import com.wellnest.one.R
import com.wellnest.one.data.local.user_pref.PreferenceManager
import com.wellnest.one.databinding.ActivityLiveRecordingBinding
import com.wellnest.realtimechart.chart.chart.*
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.recording.link.LinkRecordingActivity
import com.wellnest.one.utils.Constants
import com.wellnest.one.utils.DialogHelper
import com.wellnest.one.utils.ProgressHelper
import com.wellnest.one.utils.Util
import com.wellnest.realtimechart.chart.data.Spec
import com.wellnest.realtimechart.chart.util.EcgBackgroundDrawable
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

/**
 * Created by Hussain on 17/11/22.
 */
@AndroidEntryPoint
class BluetoothLiveEcgActivity : BaseActivity(), ISendMessageToEcgDevice, IWellnestGraph,
    View.OnClickListener, IWellnestUsbData {

    private val TAG = "BluetoothLiveEcgActivit"

    private var endSession: Boolean = false
    private lateinit var binding: ActivityLiveRecordingBinding


    private var doctorName: String = ""
    private var isTechnician = false
    private var mDeviceId = -1
    private var dataHandler = DataHandlerRunnable(this)
    var isRecording = true
    private var thread: Thread = Thread(dataHandler)

    @Inject
    lateinit var preferenceManager: PreferenceManager

    var rawQueue = ConcurrentLinkedQueue<Byte>()

    private val handler = Handler(Looper.getMainLooper())


    private var cTimer: CountDownTimer? = null
    var startTime: Long = 0
    var timeInMilliseconds: Long = 0

    lateinit var graphL1: RealTimeVitalChart
    lateinit var graphL2: RealTimeVitalChart
    lateinit var graphL3: RealTimeVitalChart
    lateinit var graphAvr: RealTimeVitalChart
    lateinit var graphAvl: RealTimeVitalChart
    lateinit var graphAvf: RealTimeVitalChart
    lateinit var graphV1: RealTimeVitalChart
    lateinit var graphV2: RealTimeVitalChart
    lateinit var graphV3: RealTimeVitalChart
    lateinit var graphV4: RealTimeVitalChart
    lateinit var graphV5: RealTimeVitalChart
    lateinit var graphV6: RealTimeVitalChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            statusBarColor =
                ContextCompat.getColor(this@BluetoothLiveEcgActivity, R.color.activity_gray_bkg)// S
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_live_recording)
        graphL1 = binding.graphL1
        graphL2 = binding.graphL2
        graphL3 = binding.graphL3
        graphAvr = binding.graphAvr
        graphAvl = binding.graphAvl
        graphAvf = binding.graphAvf
        graphV1 = binding.graphV1
        graphV2 = binding.graphV2
        graphV3 = binding.graphV3
        graphV4 = binding.graphV4
        graphV5 = binding.graphV5
        graphV6 = binding.graphV6
        Initialize()
    }

    private fun Initialize() {
        thread.start()
        wellNestUtil.startBleService(this, this, this, this, this)
        binding.imgClose.setOnClickListener(this)
        binding.imgStop.setOnClickListener(this)

//        val token = sharedPreferenceManager.getToken(this)
//        token?.let {
//            val jwt = JWT(it.token!!)
//            isTechnician = !(jwt.claims.get("type") != null && jwt.claims.get("type")!!.asString()
//                .equals("Doctor"))
//        }
//
//        val user = SharedPreferenceHelper.getUser(this)
//        if (user != null) {
//            doctorName = user.toString()
//            binding.tvReferredBy.text = doctorName
//        } else {
//            val technicianUser = SharedPreferenceHelper.getTechnicianUser(this)
//            if (technicianUser != null)
//                binding.tvReferredBy.text = technicianUser.organisation!![0].toString()
//        }

        val user = preferenceManager.getUser()?.getFullName() ?: "-"

        binding.tvReferredBy.text = user
        binding.tvHeader.text = Util.testDataTime(Date())
        binding.tvTestDate.text = Util.currentTestDate()

        wellNestUtil.clearData()

//        setUpChart(binding.graphL1, 0)

        val graphSpec = Spec(400, 3, 0.1f, 0.5f, -0.5f)
        binding.graphL1.setRealTimeSpec(graphSpec)
        binding.graphL2.setRealTimeSpec(graphSpec)
        binding.graphL3.setRealTimeSpec(graphSpec)
        binding.graphAvl.setRealTimeSpec(graphSpec)
        binding.graphAvr.setRealTimeSpec(graphSpec)
        binding.graphAvf.setRealTimeSpec(graphSpec)
        binding.graphV1.setRealTimeSpec(graphSpec)
        binding.graphV2.setRealTimeSpec(graphSpec)
        binding.graphV3.setRealTimeSpec(graphSpec)
        binding.graphV4.setRealTimeSpec(graphSpec)
        binding.graphV5.setRealTimeSpec(graphSpec)
        binding.graphV6.setRealTimeSpec(graphSpec)

        setUpChart(binding.graphL1)
        setUpChart(binding.graphL2)
        setUpChart(binding.graphL3)
        setUpChart(binding.graphAvl)
        setUpChart(binding.graphAvr)
        setUpChart(binding.graphAvf)
        setUpChart(binding.graphV1)
        setUpChart(binding.graphV2)
        setUpChart(binding.graphV3)
        setUpChart(binding.graphV4)
        setUpChart(binding.graphV5)
        setUpChart(binding.graphV6)

        val bluetoothDevice =
            preferenceManager.getBluetoothDevice()

        if (bluetoothDevice != null) {
            handler.postDelayed({
                wellNestUtil.startBluetoothLiveRecording()
                binding.tvTimer.text = "00:10"
                startDownTimer(10)
            }, 50)
        }

    }

    override fun onResume() {


        binding.graphL1.startThread()

        binding.graphL2.startThread()

        binding.graphL3.startThread()

        binding.graphAvl.startThread()

        binding.graphAvr.startThread()

        binding.graphAvf.startThread()

        binding.graphV1.startThread()

        binding.graphV2.startThread()

        binding.graphV3.startThread()

        binding.graphV4.startThread()

        binding.graphV5.startThread()

        binding.graphV6.startThread()

        super.onResume()


    }

    override fun onPause() {


        binding.graphL1.stopThread()

        binding.graphL2.stopThread()

        binding.graphL3.stopThread()

        binding.graphAvl.stopThread()

        binding.graphAvr.stopThread()

        binding.graphAvf.stopThread()

        binding.graphV1.stopThread()

        binding.graphV2.stopThread()

        binding.graphV3.stopThread()

        binding.graphV4.stopThread()

        binding.graphV5.stopThread()

        binding.graphV6.stopThread()

        super.onPause()

    }

    override fun onClick(view: View?) {
        if (view == null) return
        when (view.id) {
            R.id.imgClose -> {

                onBackPressed()
            }
            R.id.imgStop -> {
                stopDownTimer()
                stopUpTimer()
                dataHandler.running = false
                stopAllGraphs()
                thread.join()
                thread.interrupt()
                ProgressHelper.showDialog(this)
                wellNestUtil.stopBluetoothLiveRecording(true)
            }
        }
    }

    private fun stopAllGraphs() {
        binding.graphL1.dataHandler.destroy()
        binding.graphL1.destory()

        binding.graphL2.dataHandler.destroy()
        binding.graphL2.destory()

        binding.graphL3.dataHandler.destroy()
        binding.graphL3.destory()

        binding.graphAvl.dataHandler.destroy()
        binding.graphAvl.destory()

        binding.graphAvr.dataHandler.destroy()
        binding.graphAvr.destory()

        binding.graphAvf.dataHandler.destroy()
        binding.graphAvf.destory()

        binding.graphV1.dataHandler.destroy()
        binding.graphV1.destory()

        binding.graphV2.dataHandler.destroy()
        binding.graphV2.destory()

        binding.graphV3.dataHandler.destroy()
        binding.graphV3.destory()

        binding.graphV4.dataHandler.destroy()
        binding.graphV4.destory()

        binding.graphV5.dataHandler.destroy()
        binding.graphV5.destory()

        binding.graphV6.dataHandler.destroy()
        binding.graphV6.destory()
    }

    private fun setUpChart(chartView: RealTimeVitalChart) {
//        binding.imgL1.setBackgroundDrawable(EcgBackgroundDrawable(this))
        chartView.setChartBackground(EcgBackgroundDrawable(this))
        chartView.lineColor = ResourcesCompat.getColor(resources, R.color.black, null)
        chartView.lineWidth = 3.5f
        chartView.valueCircleIndicatorColor =
            ResourcesCompat.getColor(resources, R.color.black, null)
    }

    override fun onRecordingCompleted(graphList: ArrayList<ArrayList<Double>>) {
        ProgressHelper.dismissDialog()
        if (!endSession) {

            stopDownTimer()
            stopUpTimer()
            dataHandler.running = false
            thread.join()
            thread.interrupt()
            stopAllGraphs()


            if (isRecording) {
                isRecording = false
                val graphIntent: Intent =
                    Intent(this, LinkRecordingActivity::class.java)


                intent.extras?.let { graphIntent.putExtras(it) }
                graphIntent.putExtra("doctor", doctorName)
                graphIntent.putExtra("device_id", mDeviceId)
                startActivity(graphIntent)
                finish()
            }
        }
    }

    override fun addRawData(data: ByteArray) {
        synchronized(this.rawQueue) {
            rawQueue.addAll(data.toList())
        }
    }

    //start Down timer function
    fun startDownTimer(timeInSec: Int) {
        cTimer = object : CountDownTimer((timeInSec * 1000).toLong(), 100) {
            override fun onTick(millisUntilFinished: Long) {
                var secUntilFinished = (millisUntilFinished / 1000) + 1
                binding.tvTimer.text = "00:${secUntilFinished.toString().padStart(2, '0')}"
                //binding.tvTimer.setText(getDateFromMillis(millisUntilFinished))
            }

            override fun onFinish() {
                wellNestUtil.stopBluetoothLiveRecording(true)
            }
        }
        (cTimer as CountDownTimer).start()
    }

    fun stopDownTimer() {
        if (cTimer != null)
            cTimer!!.cancel()
    }

    //start Up timer function
    fun startUpTimer() {
        startTime = SystemClock.uptimeMillis()
        handler.postDelayed(updateTimerThread, 0)
    }

    fun stopUpTimer() {
        handler.removeCallbacks(updateTimerThread)
    }

    private val updateTimerThread: Runnable = object : Runnable {
        override fun run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime
            var timeInsSeconds = timeInMilliseconds / 1000
            Log.i(TAG, "$timeInsSeconds")
            if (timeInsSeconds == 10L)
                binding.imgStop.visibility = View.VISIBLE
            binding.tvTimer.setText(
                String.format(
                    "%02d:%02d",
                    (timeInsSeconds % 3600) / 60,
                    (timeInsSeconds % 60)
                )
            )
            //binding.tvTimer.setText(getDateFromMillis(timeInMilliseconds))
            handler.postDelayed(this, 1000)
        }
    }

    override fun onDeviceDisconnected() {
        super.onDeviceDisconnected()
        stopAllGraphs()
        dataHandler.running = false
        thread.join()
        thread.interrupt()
        DialogHelper.showBluetoothDisconnectedDialog(this)
    }

    override fun onBackPressed() {
        runOnUiThread {
            DialogHelper.showDialog(
                "STOP RECORDING",
                "Yes",
                "No",
                "Are you sure you want to stop ECG Recording?",
                this
            ) { _, _ ->
                stopDownTimer()
                stopUpTimer()
                endSession = true
                wellNestUtil.stopBluetoothLiveRecording(false)
                dataHandler.running = false
                stopAllGraphs()
                thread.join()
                thread.interrupt()
//                WellNestProUtil.pushLandingActivity(this@BluetoothLiveEcgActivity)
                finish()
            }
        }
    }

}

class DataHandlerRunnable(val activity: BluetoothLiveEcgActivity) : Runnable {

    private val TAG = "BluetoothLiveEcgActivit"
    private val recordByteHandler: IRecordData by lazy {
        RecordBytesFactory().getRecordBytes()
    }

    var running = true

    override fun run() {

        while (running) {

            while (!activity.rawQueue.isEmpty() && activity.rawQueue.peek()?.toInt() != -70) {
                activity.rawQueue.poll()
            }


            if (activity.rawQueue.size > 15) {
                var data = mutableListOf<Byte>()
                synchronized(activity.rawQueue) {
                    for (i in 0..15) {
                        data.add(activity.rawQueue.poll())
                    }
                }

                val finalRecordingData = ArrayList<List<Byte>>()

                finalRecordingData.add(data)
                val parsedData = recordByteHandler.setUpDataForRecording(finalRecordingData)
                if (parsedData.size > 0) {
                    activity.graphL1.dataHandler.enqueue(parsedData[0][0].toFloat())
                    activity.graphL2.dataHandler.enqueue(parsedData[0][1].toFloat())
                    activity.graphL3.dataHandler.enqueue(parsedData[0][2].toFloat())
                    activity.graphV1.dataHandler.enqueue(parsedData[0][3].toFloat())
                    activity.graphV2.dataHandler.enqueue(parsedData[0][4].toFloat())
                    activity.graphV3.dataHandler.enqueue(parsedData[0][5].toFloat())
                    activity.graphV4.dataHandler.enqueue(parsedData[0][6].toFloat())
                    activity.graphV5.dataHandler.enqueue(parsedData[0][7].toFloat())
                    activity.graphV6.dataHandler.enqueue(parsedData[0][8].toFloat())
                    activity.graphAvr.dataHandler.enqueue(parsedData[0][9].toFloat())
                    activity.graphAvl.dataHandler.enqueue(parsedData[0][10].toFloat())
                    activity.graphAvf.dataHandler.enqueue(parsedData[0][11].toFloat())
                }

            }
        }
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
}