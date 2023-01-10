package com.wellnest.one.ui

import android.app.Activity
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.messaging.FirebaseMessaging
import com.rc.wellnestmodule.interfaces.*
import com.rc.wellnestmodule.utils.RecordBytesFactory
import com.rc.wellnestmodule.utils.WellNestUtilFactory
import com.wellnest.one.BuildConfig
import com.wellnest.one.R
import com.wellnest.one.data.local.user_pref.PreferenceManager
import com.wellnest.one.utils.Util
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Created by Hussain on 07/11/22.
 */

const val RC_IMAGE_GALLERY = 1806
const val RC_TAKE_PHOTO = 1808

@AndroidEntryPoint
open class BaseActivity : PermissionHelperActivity(), IWellnestGraph, IWellNestData, ISendMessageToEcgDevice, IWellnestUsbData {

    private lateinit var photoFile : File

    lateinit var wellNestUtil: IWellNestUtil

    val recordsByteHandler by lazy {
        RecordBytesFactory().getRecordBytes()
    }

    @Inject
    lateinit var sharedPreferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        wellNestUtil = WellNestUtilFactory.getWellNestUtil()
        wellNestUtil.startBleService(this,this, this, this,this)


        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null && !TextUtils.isEmpty(task.result)) {
                        val token: String = task.result!!


                        //Here you will surely get the token so store it in
                        //sharedpreference for future use
                        sharedPreferenceManager.saveFcmToken(token)
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        wellNestUtil.onResume()
    }

    fun selectProfileImage() {
        val optionItems = arrayOf(
            getString(R.string.take_photo),
            getString(R.string.select_from_gallery),
            getString(R.string.cancel)
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Profile Image")
            .setItems(optionItems) { dialog, which ->
                when (which) {
                    0 -> {
                        // take photo from camera
                        checkCameraPermission()
                        dialog.dismiss()
                    }
                    1 -> {
                        // select image from gallery
                        selectFromGallery()
                        dialog.dismiss()
                    }
                    2 -> {
                        // cancel
                        dialog.dismiss()
                    }
                }
            }.show()
    }

    fun selectFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, RC_IMAGE_GALLERY)
        }

    }

    fun takePhoto() {
        val photoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = createImageFile()
        val photoUri = FileProvider.getUriForFile(
            this,
            "com.wellnest.one.fileprovider",
            photoFile
        )

        photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

        if (photoIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(photoIntent, RC_TAKE_PHOTO)
        } else {
            Toast.makeText(this,"No Apps Found",Toast.LENGTH_SHORT).show()
        }

    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_IMAGE_GALLERY -> {
                    val uri = data?.data ?: return
                    val inputStream = contentResolver.openInputStream(uri)
                    val bitmap =BitmapFactory.decodeStream(inputStream)
                    pickedImage(Util.orientationImage(bitmap,uri,this))
                }

                RC_TAKE_PHOTO -> {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    pickedImage(Util.orientationImage(bitmap,photoFile.toUri(),this))
                }
            }
        }
    }

    protected open fun pickedImage(bitmap : Bitmap) {
    }

    override fun cameraPermissionGranted(granted: Boolean) {
        if (granted) {
            takePhoto()
        }
    }
    fun isConnectedToInternet(mContext: Context?): Boolean {
        if (mContext == null) return false

        val connectivityManager =
            mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager != null) {
            val network = connectivityManager.activeNetwork
            if (network != null) {
                val nc = connectivityManager.getNetworkCapabilities(network)
                return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            }
        }
        return false
    }

    open fun deviceDisconnected() {
        sharedPreferenceManager.clearBluetoothDevice()
    }

    override fun sendMessage(characteristic: BluetoothGattCharacteristic, i: Int) {

    }

    override fun onDeviceConnected() {
    }

    override fun onDeviceDisconnected() {
        deviceDisconnected()
    }

    override fun onRecordingCompleted(graphList: ArrayList<ArrayList<Double>>) {
    }

    override fun setGraphView(view: View) {
    }

    override fun setBatteryStatus(batteryLevel: String) {
    }

    override fun plotGraphPoint(list: ArrayList<ArrayList<Double>>) {
    }

    override fun setDidUsb(id: String) {
    }

    override fun addRawData(data: ByteArray) {
    }

    fun sharePdf(file : Uri) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "application/pdf"
        sharingIntent.putExtra(
            Intent.EXTRA_STREAM,
            file
        )

        if (sharingIntent.resolveActivity(packageManager) != null) {
            val chooser = Intent.createChooser(sharingIntent,"Share Pdf")
            startActivity(chooser)
        } else {
            Toast.makeText(this,"Unable to share pdf",Toast.LENGTH_SHORT).show()
        }
    }

}