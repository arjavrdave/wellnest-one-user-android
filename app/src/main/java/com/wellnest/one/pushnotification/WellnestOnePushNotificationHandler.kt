package com.wellnest.one.pushnotification

import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wellnest.one.data.local.user_pref.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Created by Hussain on 08/11/22.
 */
@AndroidEntryPoint
class WellnestOnePushNotificationHandler : FirebaseMessagingService() {

    private val TAG = "WellnestOnePushNotifica"

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        preferenceManager.saveFcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.i(TAG,message.toString())
    }
}