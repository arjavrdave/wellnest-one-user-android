package com.wellnest.one.utils

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