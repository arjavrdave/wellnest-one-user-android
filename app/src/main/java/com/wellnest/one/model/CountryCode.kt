package com.wellnest.one.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CountryCode(val dialCode: String, val countryCode: String, val name: String) : Parcelable
