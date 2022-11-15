package com.wellnest.one.data.local.user_pref

import android.content.Context
import com.google.gson.Gson
import com.wellnest.one.dto.UserProfile
import com.wellnest.one.model.response.MedicalHistoryResponse
import com.wellnest.one.model.response.ProfileResponse
import com.wellnest.one.model.response.Token
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Hussain on 08/11/22.
 */
@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {

    private val sharedPreference =
        context.getSharedPreferences("WELLNEST_ONE", Context.MODE_PRIVATE)
    private val gson = Gson()


    fun saveToken(token: Token) {
        sharedPreference.edit().putString(PreferenceKeys.tokenKey, gson.toJson(token)).apply()
    }

    fun getToken(): Token? {
        val tokenString = sharedPreference.getString(PreferenceKeys.tokenKey, null) ?: return null
        val token = gson.fromJson(tokenString, Token::class.java)
        return token
    }

    fun saveFcmToken(token: String) {
        sharedPreference.edit().putString(PreferenceKeys.fcmKey, token).apply()
    }

    fun getFcmToken(): String? {
        return sharedPreference.getString(PreferenceKeys.fcmKey, null)
    }

    fun saveUser(profile: UserProfile) {
        val userJson = gson.toJson(profile)
        sharedPreference.edit().putString(PreferenceKeys.userKey, userJson).apply()
    }

    fun getUser(): UserProfile? {
        val userJson = sharedPreference.getString(PreferenceKeys.userKey, null) ?: return null
        return gson.fromJson(userJson, UserProfile::class.java)
    }

    fun clear() {
        sharedPreference.edit().clear().apply()
    }

    fun saveMedicalHistory(medicalHistory: MedicalHistoryResponse) {
        val medicalHistoryString = gson.toJson(medicalHistory)
        sharedPreference.edit().putString(PreferenceKeys.medicalHistory, medicalHistoryString)
    }

    fun getMedicalHistory(): MedicalHistoryResponse? {
        val medicalHistoryString =
            sharedPreference.getString(PreferenceKeys.medicalHistory, null) ?: return null
        return gson.fromJson(medicalHistoryString, MedicalHistoryResponse::class.java)
    }
}