package com.wellnest.one.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
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
    val weight: Int?,
    val weightUnit: String?,
    val bmi : Double,
    val dob : String
) : Parcelable {


    fun getAge() : String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dob)
            val currentDate = Date()
            (currentDate.year - date.year).toString()
        } catch (e : java.lang.Exception) {
            e.printStackTrace()
            ""
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