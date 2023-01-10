package com.wellnest.one.ui.recording.capture

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.content.*
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
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
import java.util.ArrayList
import javax.inject.Inject

/**
 * Created by Hussain on 16/11/22.
 */
@AndroidEntryPoint
class RecordingEcgActivity : BaseActivity(), ISendMessageToEcgDevice, View.OnClickListener {

    private lateinit var binding: ActivityRecordEcgBinding

    private var bluetoothLeService: BluetoothLeService? = null

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private val handler = Handler(Looper.getMainLooper())

    private var imgResources = listOf<Int>()
    private lateinit var ivArrayDotsPager: Array<ImageView>
    private lateinit var mTitles: ArrayList<String>

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

        imgResources = arrayListOf(
            R.drawable.image_electrode_guide,
            R.drawable.swipe_1,
            R.drawable.swipe_2,
            R.drawable.swipe_3
        )
        mTitles = arrayListOf(
            getString(R.string.swipe_1_txt),
            getString(R.string.swipe_2_txt),
            getString(R.string.swipe_3_txt),
            getString(R.string.swipe_4_txt)
        )

        ivArrayDotsPager = arrayOf(binding.img1, binding.img2, binding.img3, binding.img4)

        val pagerAdapter = CustomPagerAdapter(this, imgResources)
        binding.viewPager.adapter = pagerAdapter


        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            @SuppressLint("RestrictedApi")
            override fun onPageSelected(position: Int) {
                for (i in 0 until ivArrayDotsPager.size) {
                    ivArrayDotsPager[i].setImageResource(R.drawable.default_dot)
                }
                ivArrayDotsPager[position].setImageResource(R.drawable.selected_dot)
                binding.tvDesc.text = mTitles[position]

//                if (position == mTitles.size - 1) {
//                    binding.skipNextLayout.visibility = View.GONE
//                } else {
//                    binding.skipNextLayout.visibility = View.VISIBLE
//                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })


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
                val ecgCalibration = Intent(this, EcgCalibrationActivity::class.java)
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

    class CustomPagerAdapter(var context: Context, val mResources: List<Int>) :
        PagerAdapter() {
        override fun getCount(): Int {
            return mResources.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == (`object`)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val mLayoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            val itemView = mLayoutInflater.inflate(R.layout.pager_item, null)

            val imageView = itemView.findViewById(R.id.imageView) as ImageView
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true;
            imageView.setImageResource(mResources[position])


            (container as ViewPager).addView(itemView)

            return itemView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            (container as ViewPager).removeView(`object` as View);

        }

        fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int,
            reqHeight: Int
        ): Int {
            // Raw height and width of image
            val (height: Int, width: Int) = options.run { outHeight to outWidth }
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {

                val halfHeight: Int = height / 2
                val halfWidth: Int = width / 2

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            return inSampleSize
        }

        fun decodeSampledBitmapFromResource(
            res: Resources,
            resId: Int,
            reqWidth: Int,
            reqHeight: Int
        ): Bitmap {
            // First decode with inJustDecodeBounds=true to check dimensions
            return BitmapFactory.Options().run {
                inJustDecodeBounds = true
                BitmapFactory.decodeResource(res, resId, this)

                // Calculate inSampleSize
                inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

                // Decode bitmap with inSampleSize set
                inJustDecodeBounds = false

                BitmapFactory.decodeResource(res, resId, this)
            }
        }
    }

}