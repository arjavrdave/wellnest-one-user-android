package com.wellnest.one

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

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
}