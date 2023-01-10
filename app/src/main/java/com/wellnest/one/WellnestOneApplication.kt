package com.wellnest.one

import android.app.Application
import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.hilt.android.HiltAndroidApp
import org.json.JSONObject

/**
 * Created by Hussain on 07/11/22.
 */
@HiltAndroidApp
class WellnestOneApplication : Application() {
    companion object {
        private var instance: WellnestOneApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val config = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(1000 * 60 * 5) // 5 minutes
            .build()

        val params =
            JSONObject("{\"active\":true,\"messages\":[\"Processing your request\",\"Checking your device connection\",\"Securing network\",\"Checking electrode connections\",\"Fetching data\",\"Calibrating the device\",\"Encrypting data\",\"Getting your device ready\"],\"msgTime\":2000,\"successTime\":300,\"errorMsg\":\"Please verify your connections!\",\"threshold\":-0.8}")
        val defaults = HashMap<String, String>()
        defaults["ECGChecklist"] = params.toString()


        remoteConfig.setConfigSettingsAsync(config)
        remoteConfig.setDefaultsAsync(defaults as Map<String, Any>)
        remoteConfig.fetchAndActivate()
    }
}