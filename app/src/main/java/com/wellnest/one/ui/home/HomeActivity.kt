package com.wellnest.one.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import com.rc.wellnestmodule.BluetoothLeService
import com.wellnest.one.R
import com.wellnest.one.data.local.user_pref.PreferenceManager
import com.wellnest.one.databinding.ActivityHomeBinding
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.profile.UserProfileActivity
import com.wellnest.one.ui.recording.pair.PairDeviceActivity
import com.wellnest.one.ui.recording.pair.SymptomsActivity
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject

/**
 * Created by Hussain on 08/11/22.
 */
@AndroidEntryPoint
class HomeActivity : BaseActivity(), View.OnClickListener {

    private val TAG = "HomeActivity"

    private lateinit var binding: ActivityHomeBinding

    private var bluetoothLeService: BluetoothLeService? = null

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        binding.imgSettings.setOnClickListener(this)
        binding.btnRecording.setOnClickListener(this)

        val fcmtoken = preferenceManager.getFcmToken()
        Log.i(TAG, "$fcmtoken")

    }

    override fun onResume() {
        super.onResume()
        if (bluetoothLeService != null) {
            setBluetoothState()
        } else {
            val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
            bindService(
                gattServiceIntent,
                mServiceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    private fun setBluetoothState() {
//        if (!checkBluetoothPermissions()) return

//        val bluetoothDevice = preferenceManager.getBluetoothDevice()
//
//        if (bluetoothLeService?.isConnected() == false) {
//            val eventProp = JSONObject()
//            eventProp.put("ecgDeviceId", bluetoothDevice?.deviceId)
//
//            //eventProp.put("patientId", patient.id)
//            Amplitude.getInstance().logEvent("Start Pairing (New Recording)", eventProp)
//
//            isBluetoothDeviceConnected = false
//            return
//        } else {
//            isBluetoothDeviceConnected = true
//            isUsbDeviceConnected = false
//            return
//        }
    }

    val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            bluetoothLeService = (service as BluetoothLeService.LocalBinder).service
            if (!bluetoothLeService!!.initialize()) {
                //  finish()
            }
            setBluetoothState()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothLeService = null
            //WellNestLoader.dismissLoader()
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgSettings -> {
                startActivity(Intent(this, UserProfileActivity::class.java))
            }

            R.id.btnRecording -> {
                if (bluetoothLeService?.isConnected() == true) {
                    val symptoms = Intent(this, SymptomsActivity::class.java)
                    startActivity(symptoms)
                } else {
                    val pairDeviceIntent = Intent(this, PairDeviceActivity::class.java)
                    pairDeviceIntent.putExtra("isHomePage", true)
                    pairDeviceIntent.putExtra("prepareRecording", true)
                    startActivity(pairDeviceIntent)
                }
            }
        }
    }
}