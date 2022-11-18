package com.wellnest.one.utils

import android.app.Activity
import android.content.Intent
import com.wellnest.one.ui.home.HomeActivity

object WellNestProUtil {
    fun pushLandingActivity(activity: Activity) {
        val intent =  Intent(activity, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        activity.startActivity(intent)
        activity.finish()
    }

}
