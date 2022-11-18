package com.wellnest.one.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature
import com.microsoft.azure.storage.blob.CloudBlockBlob
import com.wellnest.one.BuildConfig
import com.wellnest.one.R
import com.wellnest.one.model.CountryCode
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Hussain on 07/11/22.
 */
object Util {

    fun getCountryCode(context: Context): List<CountryCode>? {
        val countryCodeList: MutableList<CountryCode> = ArrayList()
        try {
            val jsonDataString = readJSONDataFromFile(context)
            val jsonArray = JSONArray(jsonDataString)
            for (i in 0 until jsonArray.length()) {
                val itemObj = jsonArray.getJSONObject(i)
                val name = itemObj.getString("name")
                val countryCode = itemObj.getString("code")
                val dial_code = itemObj.getString("dial_code")
                val data = CountryCode(dial_code, countryCode, name)
                countryCodeList.add(data)
            }
        } catch (e: JSONException) {
            Log.d("getcountrycode", "addItemsFromJSON: ", e)
        } catch (e: IOException) {
            Log.d("getcountrycode", "addItemsFromJSON: ", e)
        }
        return countryCodeList
    }

    @Throws(IOException::class)
    fun readJSONDataFromFile(context: Context): String {
        var inputStream: InputStream? = null
        val builder = StringBuilder()
        try {
            var jsonString: String? = null
            inputStream = context.resources.openRawResource(R.raw.country_code)
            val bufferedReader = BufferedReader(
                InputStreamReader(inputStream, "UTF-8")
            )
            while (bufferedReader.readLine().also { jsonString = it } != null) {
                builder.append(jsonString)
            }
        } finally {
            inputStream?.close()
        }
        return String(builder)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun orientationImage(bitmap: Bitmap, uri: Uri, context: Context): Bitmap {
        try {
            val ips = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exifInterface = ExifInterface(ips)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    rotateImage(bitmap, 90)
                }
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    rotateImage(bitmap, 180)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    rotateImage(bitmap, 270)
                }
                else -> {
                    bitmap
                }
            }
        } catch (e: IOException) {
            e.localizedMessage?.let { FirebaseCrashlytics.getInstance().log(it) }
        }


        return bitmap
    }

    private fun rotateImage(source: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())

        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }


    @Throws(Exception::class)
    fun UploadImage(
        image: InputStream?,
        imageLength: Int,
        container: String,
        id: String,
        sasToken: String?
    ) {
        val storageCredentials = StorageCredentialsSharedAccessSignature(sasToken)
        val cloudBlockBlob = CloudBlockBlob(
            URI.create(BuildConfig.azureHOST + container + "/" + id),
            storageCredentials
        )
        cloudBlockBlob.upload(image, imageLength.toLong())
    }

    fun GetImage(id: String, container: String, sasToken: String?): Bitmap? {
        try {
            val storageCredentials = StorageCredentialsSharedAccessSignature(sasToken)
            val cloudBlockBlob = CloudBlockBlob(
                URI.create(BuildConfig.azureHOST + container + "/" + id),
                storageCredentials
            )
            val byteArray: ByteArray = getByteArray(cloudBlockBlob)!!
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getByteArray(blockBlobReference: CloudBlockBlob): ByteArray? {
        try {
            blockBlobReference.downloadAttributes()
            val imageBuffer = ByteArray(
                blockBlobReference.properties.length
                    .toInt()
            )
            blockBlobReference.downloadToByteArray(imageBuffer, 0)
            return imageBuffer
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun isoToDobString(isoDateString: String): String {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(isoDateString)
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.US)
        return sdf.format(date)
    }


    fun testDataTime(createdAt: Date): String? {
        val date = SimpleDateFormat("dd MMM, yyyy")
        return date.format(createdAt)
    }

    fun currentTestDate(): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy | hh:mm:ss")
        val strDate = dateFormat.format(Date())
        return strDate
    }

    fun currentTestDateAmPm(): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy | hh:mm a")
        val strDate = dateFormat.format(Date())
        return strDate.replace("am", "AM").replace("pm", "PM")
    }

}