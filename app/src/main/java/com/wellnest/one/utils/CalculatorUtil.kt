package com.wellnest.one.utils

import com.wellnest.one.utils.Constants.FEET_FACTOR
import com.wellnest.one.utils.Constants.GRAM_FACTOR
import com.wellnest.one.utils.Constants.INCH_FACTOR
import com.wellnest.one.utils.Constants.METER_FACTOR
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * Created by Hussain on 11/11/22.
 */
object CalculatorUtil {

    fun feetToCms(feet: String, inch: String): Double {
        val heightInCms = feet.toFloat() * FEET_FACTOR + inch.toFloat() * INCH_FACTOR
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING

        return df.format(heightInCms).toDouble()
    }

    fun cmsToFeet(cms: Double): Double {

        val feet = (cms.toFloat() / 100) * METER_FACTOR
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING

        return df.format(feet).toDouble()
    }

    fun kilosToGrams(kilo: String, gram: String): Double {

        val weight = kilo.toFloat() * 1000 + gram.toFloat()

        return weight.toDouble()
    }

    fun formattedHeight(height: Double): Double {
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        return df.format(height).toDouble()
    }

    fun formattedTemp(temp: Double): Double {
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        return df.format(temp).toDouble()
    }

    fun gramsToKilos(weight: Double): Double {
        return weight / 1000
    }

    fun formattedGrams(weight: Double): Double {
        val kilos = gramsToKilos(weight)
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        return df.format(kilos).toDouble()
    }

    fun gramsToPounds(weight: Double): Double {
        return weight / GRAM_FACTOR
    }

    fun poundsToGrams(pounds: String): Double {
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        return df.format(pounds.toDouble() * GRAM_FACTOR).toDouble()
    }

}