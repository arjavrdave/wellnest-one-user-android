package com.wellnest.one.pushnotification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wellnest.one.R
import com.wellnest.one.data.local.user_pref.PreferenceManager
import com.wellnest.one.ui.feedback.ECGFeedbackActivity
import com.wellnest.one.ui.feedback.FeedbackStatus
import com.wellnest.one.ui.home.HomeActivity
import com.wellnest.one.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

/**
 * Created by Hussain on 08/11/22.
 */
@AndroidEntryPoint
class WellnestOnePushNotificationHandler : FirebaseMessagingService() {

    private val TAG = "WellnestOnePushNotifica"
    private val CHANNEL_ID = "Wellnest One"


    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        preferenceManager.saveFcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.i(TAG,message.data.toString())

        /* {
            UserId=1421,
            PendingApproval=False,
            OrganisationId=183,
            ReviewStatus=FeedbackGiven,
            Id=4955,
            Type=RECORDING_FEEDBACK,
            title=ECG REPORT COMPLETED,
            ECGRecordingId=9411,
            DoctorId=70,
            message=Hussain : ECG report has been generated
           }

         */
        if (message.data.isNotEmpty()) {
            val recordingId = message.data["ECGRecordingId"]
            val title = message.data["title"]
            val desc = message.data["message"]
            val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val random = Random()
            val requestCode =  random.nextInt()
            val intent =  Intent(applicationContext,ECGFeedbackActivity::class.java)
            intent.putExtra("id",recordingId?.toInt())
            intent.putExtra("from","pushnotification")
            intent.putExtra("status",FeedbackStatus.AnalysisReceived.ordinal)
            val pendingIntent = PendingIntent.getActivity(this,requestCode,intent,pendingFlags)
            sendNotification(pendingIntent,title!!,desc!!)
        }
    }

    private fun sendNotification(pendingIntent: PendingIntent, title: String, message: String) {

        val random = Random()
        val id = random.nextInt()


        val nm =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val CHANNEL_ID = CHANNEL_ID
            val name: CharSequence = getString(R.string.channel_name)
            val Description = ""
            val importance = NotificationManager.IMPORTANCE_HIGH
            // Configure the notification channel.
            val att = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = Description
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.setSound(defaultSoundUri, att)
            mChannel.enableVibration(true)
            mChannel.setShowBadge(true)
            nm.createNotificationChannel(mChannel)
        }


        var build: Notification? = null
        build = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.one_logo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentText(message)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()


        nm.notify(id, build)
    }

}