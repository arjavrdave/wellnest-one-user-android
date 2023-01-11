package com.wellnest.one.dto

import android.os.Build
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.util.*

/**
 * Created by Hussain on 14/11/22.
 */
@Parcelize
data class UserProfile(
    val countryCode: Int?,
    val email: String?,
    val exerciseLevel: String?,
    val firstName: String?,
    val gender: String?,
    val height: Double?,
    val heightUnit: String?,
    val id: Int?,
    val lastName: String?,
    val phoneNumber: String?,
    val smoking: String?,
    val tobaccoUse: String?,
    val weight: Double?,
    val weightUnit: String?,
    val bmi: Double?,
    val dob: String?,
    val profileId: String
) : Parcelable {


    fun getAge(): String {
        return try {
            val getFormat: Date =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH).parse(dob!!)!!
            val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)
            val monthFormat = SimpleDateFormat("MM", Locale.ENGLISH)
            val dayFormat = SimpleDateFormat("dd", Locale.ENGLISH)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Period.between(
                    LocalDate.of(
                        yearFormat.format(getFormat).toInt(),
                        monthFormat.format(getFormat).toInt(),
                        dayFormat.format(getFormat).toInt()
                    ), LocalDate.now()
                ).years.toString()
            } else {
                val currentDate = Date()
                (currentDate.year - getFormat.year).toString()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            "0"
        }
    }

    fun getFullName(): String {
        return if (firstName != null && lastName != null) {
            "$firstName $lastName"
        } else if (firstName != null) {
            "$firstName"
        } else if (lastName != null) {
            "$lastName"
        } else {
            ""
        }
    }
}