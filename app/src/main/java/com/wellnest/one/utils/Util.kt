package com.wellnest.one.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.format.DateFormat
import android.util.Log
import android.util.TypedValue
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature
import com.microsoft.azure.storage.blob.CloudBlockBlob
import com.wellnest.one.BuildConfig
import com.wellnest.one.R
import com.wellnest.one.model.CountryCode
import com.wellnest.one.model.response.Patient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import java.io.*
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

    fun getGenderAge(gender: String?, dob: String?): String {

        var genderAgeS: StringBuilder = java.lang.StringBuilder("")

        genderAgeS.append(gender?.let { it.toLowerCase(Locale.ROOT).capitalize(Locale.ROOT) } ?: "-")
        genderAgeS.append(" | ")
        genderAgeS.append("${dob?.let { getYears(it) } ?: "-"}  Years")

        return genderAgeS.toString()
    }

    fun getYears(dob: String): Int {
        val date = isoToDate(dob)
        val day = DateFormat.format("dd", date).toString()// gets date
        val month = DateFormat.format("MM", date).toString() //gets month
        val year = DateFormat.format("yyyy", date).toString() //gets year

        return getAge(year.toInt(), month.toInt(), day.toInt())!!.toInt()
    }

    private fun getAge(year: Int, month: Int, day: Int): String? {
        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()
        if (month == 1) {
            dob.set(year, 0, day)
        } else {
            dob.set(year, month - 1, day)
        }

        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        val ageInt = age
        return ageInt.toString()
    }

    fun isoToDate(value: String): Date {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(value)
        return date
    }

    fun toDP(context: Context, value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(), context.resources.displayMetrics
        ).toInt()
    }

    fun isoToLocalDate(value: String): Date {
        var df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH)
        df.timeZone = TimeZone.getTimeZone("UTC")
        var date: Date
        try {
            date = df.parse(value)
        } catch (e: java.lang.Exception) {
            df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            df.timeZone = TimeZone.getTimeZone("UTC")
            date = df.parse(value)
        }
        df.timeZone = TimeZone.getDefault()
        val formatLocal = df.format(date)
        return df.parse(formatLocal)
    }

    fun testDateAmPm(createdAt: String): String {
        val date = isoToLocalDate(createdAt)
        val dateFormat = SimpleDateFormat("MMM dd, yyyy | hh:mm a")

        var strDate = ""
        try {
            strDate = dateFormat.format(date)
        } catch (e: java.lang.Exception) {

        }
        return strDate.replace("am", "AM").replace("pm", "PM")
    }

    fun loadImage(activityContext: Context, id: String, sasToken: String, view: ImageView) {
        Glide.with(activityContext)
            .load(BuildConfig.azureHOST + Constants.PROFILE_IMAGES + "/${id}" + sasToken)
            .placeholder(
                R.drawable.ic_user
            ).into(view)
    }

    fun createPdf(bitmap: Bitmap,patient: Patient?,createdAt: String,context: Context) : Uri? {
        val pdfDocument = PdfDocument();
        val paint = Paint()

        var uri: Uri? = null

        try {

            val pageInfo: PdfDocument.PageInfo
            val page: PdfDocument.Page?
            val canvas: Canvas

            val resizedBitmap =  scaleBitmap(bitmap,context) ?: return null
            pageInfo = PdfDocument.PageInfo.Builder(resizedBitmap.width,resizedBitmap.height,1).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas

            canvas.drawBitmap(resizedBitmap,0f,0f,paint)
            pdfDocument.finishPage(page)

            val patientName = patient?.fullName() ?: ""
            val testDate = testDateAmPm(createdAt)

//            val direct = File(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
//                "Wellnest-One"
//            )
//
//            if (!direct.exists()) {
//                val wellnest12lDirect = File(
//                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
//                    "Wellnest-One"
//                )
//                wellnest12lDirect.mkdirs()
//            }

//            val directPath =
//                "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)!!.absolutePath}/Wellnest-One"


            try {
                val fileName = "ECGReport_" + patientName + "_" + testDate + ".pdf"

                val outputStream: OutputStream?
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues()
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName) // file name
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents")
                    val extVolumeUri: Uri = MediaStore.Files.getContentUri("external")
                    val fileUri: Uri? = context.contentResolver.insert(extVolumeUri, values)
                    outputStream = context.contentResolver.openOutputStream(fileUri!!)
                    uri = fileUri
                } else {
                    val path =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                            .toString() + "Wellnest-One"
                    val file = File(path, fileName)
                    outputStream = FileOutputStream(file)
                    uri = file.toUri()
                }
                pdfDocument.writeTo(outputStream)
                outputStream?.close()
//                file = File(directPath, fileName)
//                val stream = FileOutputStream(file)
//                pdfDocument.writeTo(stream)
                pdfDocument.close()
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

        return uri
    }

    fun scaleBitmap(bm: Bitmap,context: Context): Bitmap? {
        // scale to a4 paper size 297 mm x 210 mm
        val maxWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM,297f,context.resources.displayMetrics).toInt()
        val maxHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM,210f,context.resources.displayMetrics).toInt()
        val  newbm = Bitmap.createScaledBitmap(bm, maxWidth, maxHeight, true)
        return newbm
    }

    fun formatPhone(phone:String?,countryCode:String?):String{
        if (phone == null){
            return ""
        }

        val builder = java.lang.StringBuilder()

        if(countryCode=="") {
            builder.append("+91 ")
        }
        else{
            builder.append("+$countryCode ")
        }

        builder.append(phone.substring(0,5))
        builder.append(" ")
        builder.append(phone.substring(5))
        return builder.toString()
    }


}