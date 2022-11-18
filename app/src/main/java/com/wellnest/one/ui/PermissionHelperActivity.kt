package com.wellnest.one.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * Created by Hussain on 07/11/22.
 */
open class PermissionHelperActivity : AppCompatActivity() {

    private lateinit var coarseLocationPermLaucher: ActivityResultLauncher<String>
    private lateinit var fineLocationPermLaucher: ActivityResultLauncher<String>

    var locationPermissionRequest =
        registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions(),
            ActivityResultCallback<Map<String, Boolean>> { result: Map<String, Boolean> ->
                val fineLocationGranted = result.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION, false
                )
                val coarseLocationGranted = result.getOrDefault(
                    Manifest.permission.ACCESS_COARSE_LOCATION, false
                )
                if (fineLocationGranted != null && fineLocationGranted) {
                    // Precise location access granted.
                    startLocationServices(true)
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    // Only approximate location access granted.
                    startLocationServices(true)
                } else {
                    // No location access granted.
                    val settingPageIntent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri =
                        Uri.fromParts("package", packageName, null)
                    settingPageIntent.data = uri
                    startActivity(settingPageIntent)
                }
            }
        )

    var bluetoothConnectScanRequest =
        registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions(),
            ActivityResultCallback<Map<String, Boolean>> { result: Map<String, Boolean> ->
                val bleConnect = result.getOrDefault(
                    Manifest.permission.BLUETOOTH_CONNECT, false
                )
                val bleScan = result.getOrDefault(
                    Manifest.permission.BLUETOOTH_SCAN, false
                )
                if (bleConnect != null && bleConnect) {
                    startBluetoothService(true)
                } else if (bleScan != null && bleScan) {
                    startBluetoothService(true)
                } else {
                    val settingPageIntent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri =
                        Uri.fromParts("package", packageName, null)
                    settingPageIntent.data = uri
                    startActivity(settingPageIntent)
                }
            }
        )

    open fun startBluetoothService(granted: Boolean) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        coarseLocationPermLaucher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startLocationServices(true)
            } else {
//                Intent settingPageIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                Uri uri = Uri.fromParts("package",getPackageName(),null);
//                settingPageIntent.setData(uri);
//                startActivity(settingPageIntent);
            }
        }

        fineLocationPermLaucher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startLocationServices(true)
            } else {
                val settingPageIntent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri =
                    Uri.fromParts("package", packageName, null)
                settingPageIntent.data = uri
                startActivity(settingPageIntent)
            }
        }
    }

    protected open fun checkBluetoothPermissions(): Boolean? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothConnectScanRequest.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
            false
        } else {
            startBluetoothService(true)
            true
        }
    }

    protected open fun checkCoarseLocationPermission() {
        coarseLocationPermLaucher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    protected open fun checkFineLocationPermission() {
        fineLocationPermLaucher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    protected open fun checkFineCoarseLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    open fun startLocationServices(granted: Boolean) {

    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

}