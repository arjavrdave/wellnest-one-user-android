package com.wellnest.one.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Patterns
import android.view.View
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * Created by Hussain on 09/11/22.
 */

fun Double.convertFeetToCms():Double{
    return this* Constants.FEET_FACTOR
}

fun Double.convertInchToCms():Double{
    return this * Constants.INCH_FACTOR
}

fun Double.convertCmsToInch():Double{
    return this/Constants.INCH_FACTOR
}

fun Double.roundValue(): Double {
    val df = DecimalFormat("#")
    df.roundingMode = RoundingMode.CEILING
    return df.format(this).toDouble()
}

fun Double.convertKilosToPounds():Double{
    return this/ Constants.POUND_FACTOR
}

fun Double.convertPoundsToKilos():Double{
    return this * Constants.POUND_FACTOR
}


fun Double.formatValue(): Double {
    val df = DecimalFormat("#.#")
    df.roundingMode = RoundingMode.FLOOR
    return df.format(this).toDouble()
}

fun String?.isValidEmail(): Boolean = Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun View.generateBitmap(): Bitmap {
    return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
        Canvas(this).apply {
            this@generateBitmap.draw(this)
        }
    }
}

fun String.isAlphaNumeric(allowDot : Boolean = false) : Boolean {
    val validChars = mutableListOf<Char>()
    validChars.addAll('A'..'Z')
    validChars.addAll('a'..'z')
    validChars.addAll('0'..'9')
    if (allowDot) validChars.add('.')

    for (n in this) {
        if (n != ' ' && !validChars.contains(n)) {
            return false
        }
    }
    return true
}

fun String.isNumeric() : Boolean {
    val validChars = mutableListOf<Char>()
    validChars.addAll('0'..'9')
    for (n in this) {
        if (!validChars.contains(n)) {
            return false
        }
    }
    return true
}
