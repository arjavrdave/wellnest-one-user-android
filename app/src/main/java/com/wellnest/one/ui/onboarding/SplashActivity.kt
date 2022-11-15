package com.wellnest.one.ui.onboarding

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.wellnest.one.R
import com.wellnest.one.data.local.user_pref.PreferenceManager
import com.wellnest.one.databinding.ActivitySplashBinding
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.home.HomeActivity
import com.wellnest.one.ui.profile.CreateEditProfileActivity
import com.wellnest.one.utils.Constants.SPLASH_ANIMATION_DURATION
import com.wellnest.one.utils.Constants.TIME_DELAY_3SEC
import com.wellnest.one.utils.Constants.fromXValue
import com.wellnest.one.utils.Constants.toXValue
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Hussain on 07/11/22.
 */
@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    private lateinit var binding : ActivitySplashBinding

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = ResourcesCompat.getColor(resources,R.color.transparent,null)
        window.navigationBarColor = ResourcesCompat.getColor(resources,R.color.transparent,null)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        lifecycleScope.launch {
            val translateAnimation: Animation

            translateAnimation = TranslateAnimation(
                TranslateAnimation.ABSOLUTE,
                fromXValue,
                TranslateAnimation.ABSOLUTE,
                toXValue,
                TranslateAnimation.ABSOLUTE,
                0f,
                TranslateAnimation.ABSOLUTE,
                0f
            )

            translateAnimation.setDuration(SPLASH_ANIMATION_DURATION)
            translateAnimation.fillAfter = true
            translateAnimation.setInterpolator(LinearInterpolator())
            binding.imgWellnest.startAnimation(translateAnimation)

            delay(TIME_DELAY_3SEC)

            continueLogin()
        }
    }

    private fun continueLogin() = runOnUiThread {
        val token = preferenceManager.getToken()

        if (token == null) {
            startActivity(Intent(this,WelcomeActivity::class.java))
            finish()
        } else if (token.isNewUser == true){
            startActivity(Intent(this,CreateEditProfileActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this,HomeActivity::class.java))
            finish()
        }
    }
}