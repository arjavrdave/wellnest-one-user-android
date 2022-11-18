package com.wellnest.one.ui.recording.pair

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.location.LocationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.wellnest.one.R
import com.wellnest.one.databinding.ActivityPairDeviceBinding
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.home.HomeActivity
import com.wellnest.one.utils.DialogHelper
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by Hussain on 16/11/22.
 */
@AndroidEntryPoint
class PairDeviceActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityPairDeviceBinding
    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter
    private val REQUEST_CONNECT_DEVICE = 1
    private val REQUEST_ENABLE_LOCATION = 205

    private var isInternetAvailable = false
    private var locationPermissionGranted = false
    private var blePermissionGranted = false
    private var gpsTurnedOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pair_device)
        init()
    }

    fun init() {
        binding.lifecycleOwner = this
        binding.btnNext.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        binding.imgClose.setOnClickListener(this)

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = mSectionsPagerAdapter
        mSectionsPagerAdapter.addFragment(WellnestDeviceFragment())
        mSectionsPagerAdapter.addFragment(DevicesFragment())
        binding.viewPager.offscreenPageLimit = 2

        binding.viewPager.setOnTouchListener { v, event -> true }

        if (isConnectedToInternet(applicationContext)) {
            isInternetAvailable = true
        }

        checkPermissions()
//        checkBluetoothPermissions()
//        checkCoarseLocationPermission()
//        checkFineLocationPermission()
//        checkGPSStatus()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // ask for ble scan and connect
            checkFineCoarseLocationPermission()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ask for fine location permission
            checkFineLocationPermission()
        } else {
            // ask for coarse location permission
            checkCoarseLocationPermission()
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnNext -> {
                if (locationPermissionGranted && blePermissionGranted && locationPermissionGranted) {
                    val currentItem = binding.viewPager.currentItem
                    if (currentItem == 0) {
                        var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (!bluetoothAdapter.isEnabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                checkBluetoothPermissions()
                            }
                        } else {
                            try {
                                secondPage()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                    }
                } else {
                    checkPermissions()
                }
            }

            R.id.imgBack -> {
                binding.viewPager.setCurrentItem(0)
                binding.btnNext.setText("NEXT")
                binding.tvHint.setText(getString(R.string.Press_hold))
                binding.imgBack.visibility = View.INVISIBLE
                binding.btnNext.visibility = View.VISIBLE
                firstPage()
            }

            R.id.imgClose -> {
                if (intent.getBooleanExtra("prepareRecording", false)) {
                    finish()
                } else {
                    val homeIntent = Intent(this, HomeActivity::class.java)
                    homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(homeIntent)
                    finish()
                }
            }
        }
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: androidx.fragment.app.FragmentManager) :
        androidx.fragment.app.FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val mFragmentList = ArrayList<Fragment>()

        override fun getItem(position: Int): Fragment {
            val bundle = Bundle()
            bundle.putInt("index", position)
            bundle.putBoolean("prepareRecording", intent.getBooleanExtra("prepareRecording", false))
            bundle.putBoolean("isHomePage", intent.getBooleanExtra("isHomePage", false))
            bundle.putBoolean("atLogin", intent.getBooleanExtra("atLogin", false))
            bundle.putBoolean("is_connected", isInternetAvailable)
            mFragmentList.get(position).setArguments(bundle)
            return mFragmentList.get(position)

        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        override fun getItemPosition(`object`: Any): Int {
            return androidx.viewpager.widget.PagerAdapter.POSITION_NONE
        }

        fun addFragment(fragment: Fragment) {
            mFragmentList.add(fragment)
            notifyDataSetChanged()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CONNECT_DEVICE -> {
                    if (resultCode == Activity.RESULT_OK) {
//                        secondPage()
                    }
                }
                REQUEST_ENABLE_LOCATION -> {
                    //recheck GPS settings
                    checkGPSStatus()
                }
            }
        }
    }

    fun secondPage() {
        binding.viewPager.setCurrentItem(1)
        binding.imgBack.visibility = View.VISIBLE
        binding.btnNext.visibility = View.GONE
        binding.tvHint.setText(getString(R.string.select_wellnest))
        binding.btnNext.visibility = View.GONE

        binding.viewPager.adapter?.let {
            try {
                val devicesFragment = it.instantiateItem(binding.viewPager, 1) as DevicesFragment
                devicesFragment.startBluetoothDiscovery()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun firstPage() {
        binding.viewPager.currentItem = 0
        binding.btnNext.text = "NEXT"
        binding.tvHint.text = getString(R.string.Press_hold)
        binding.btnNext.visibility = View.VISIBLE
    }


    private fun checkGPSStatus() {

        var gpsEnabled: Boolean
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        gpsEnabled = LocationManagerCompat.isLocationEnabled(locationManager)

        if (!gpsEnabled) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Location Service")
            builder.setMessage("Wellnest 12L needs your location access in order to function!")
            builder.setCancelable(false)
            builder.setPositiveButton(
                android.R.string.ok
            ) { p0, p1 ->
                startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    REQUEST_ENABLE_LOCATION
                )
            }
            builder.show()
        } else {
            gpsTurnedOn = true
        }
    }

    @SuppressLint("MissingPermission")
    override fun startBluetoothService(granted: Boolean) {
        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        if (granted) {
            blePermissionGranted = true
            startActivityForResult(enableIntent, REQUEST_CONNECT_DEVICE)
            checkGPSStatus()
        } else
            DialogHelper.showDialog(
                "Permission Required",
                "In order to connect to Wellnest 12L Devices bluetooth permission is required",
                this
            ) { dialog, _ ->
                val settingPageIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                settingPageIntent.data = uri
                startActivity(settingPageIntent)
                dialog.dismiss()
            }
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun startLocationServices(granted: Boolean) {
        if (granted) {
            locationPermissionGranted = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkBluetoothPermissions()
            } else {
                blePermissionGranted = true
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableIntent, REQUEST_CONNECT_DEVICE)
                checkGPSStatus()
            }
        } else {
            DialogHelper.showDialog(
                "Permission Required",
                "In order to scan  Wellnest 12L Devices Location permission is required",
                this
            ) { dialog, _ ->
                val settingPageIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                settingPageIntent.data = uri
                startActivity(settingPageIntent)
                dialog.dismiss()
            }
        }
    }
}
