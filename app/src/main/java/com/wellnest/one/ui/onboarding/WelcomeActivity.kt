package com.wellnest.one.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.wellnest.one.R
import com.wellnest.one.databinding.ActivityWelcomeBinding
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.login_signup.LoginActivity

/**
 * Created by Hussain on 07/11/22.
 */
class WelcomeActivity : BaseActivity() {

    private lateinit var binding : ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ResourcesCompat.getColor(resources,R.color.color_surface_grey,null)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome)

        binding.btnLogin.setOnClickListener {
            val loginIntent = Intent(this,LoginActivity::class.java)
            startActivity(loginIntent)
        }

        binding.tvGetInTouch.setOnClickListener {

        }

    }
}