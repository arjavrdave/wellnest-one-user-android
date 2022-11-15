package com.wellnest.one.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wellnest.one.R
import com.wellnest.one.utils.Util
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Hussain on 07/11/22.
 */

const val RC_IMAGE_GALLERY = 1806
const val RC_PERM_EXT = 1807
const val RC_TAKE_PHOTO = 1808

open class BaseActivity : PermissionHelperActivity() {

    private lateinit var photoFile : File

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
                        takePhoto()
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

        startActivityForResult(photoIntent, RC_TAKE_PHOTO)

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


}