package com.wellnest.one.ui.termofservice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.wellnest.one.R
import com.wellnest.one.databinding.ActivityTermsOfServiceBinding

class TermsOfServiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTermsOfServiceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_terms_of_service)
        binding.pdfView.fromStream(resources.openRawResource(R.raw.wellnest_tos)).load()
        binding.imgBack.setOnClickListener {
            finish()
        }
    }
}