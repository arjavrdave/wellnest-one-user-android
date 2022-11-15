package com.wellnest.one.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.blongho.country_data.World
import com.bumptech.glide.Glide

/**
 * Created by Hussain on 07/11/22.
 */

@BindingAdapter("android:country_initials")
fun loadFlag(imageView: ImageView, initials:String){
    val countryFlagId = World.getFlagOf(initials)
    imageView.setImageResource(countryFlagId)
}