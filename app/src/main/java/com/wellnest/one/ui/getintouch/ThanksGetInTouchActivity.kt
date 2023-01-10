package com.wellnest.one.ui.getintouch

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.wellnest.one.R
import com.wellnest.one.databinding.ActivityThanksGetInTouchBinding

class ThanksGetInTouchActivity : AppCompatActivity() {

    private lateinit var binding : ActivityThanksGetInTouchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            statusBarColor =
                ContextCompat.getColor(this@ThanksGetInTouchActivity, R.color.activity_gray_bkg)// S
        }


        binding = DataBindingUtil.setContentView(this,R.layout.activity_thanks_get_in_touch)

        binding.imgClose.setOnClickListener {
            finish()
        }

        binding.tvGetInTouch.setOnClickListener {
            val url = "https://wellnest.tech"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
    }
}