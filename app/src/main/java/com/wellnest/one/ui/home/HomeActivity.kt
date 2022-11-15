package com.wellnest.one.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import com.wellnest.one.R
import com.wellnest.one.data.local.user_pref.PreferenceManager
import com.wellnest.one.databinding.ActivityHomeBinding
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.profile.UserProfileActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Created by Hussain on 08/11/22.
 */
@AndroidEntryPoint
class HomeActivity : BaseActivity(), View.OnClickListener {

    private val TAG = "HomeActivity"

    private lateinit var binding : ActivityHomeBinding

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        binding.imgSettings.setOnClickListener(this)

        val fcmtoken = preferenceManager.getFcmToken()
        Log.i(TAG,"$fcmtoken")

    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.imgSettings -> {
                startActivity(Intent(this, UserProfileActivity::class.java))
            }
        }
    }
}