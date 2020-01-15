package edu.ba.twoDimensionalRLE.extensions

import java.math.BigDecimal
import java.math.RoundingMode

fun Double.isWholeNumber(): Boolean {
    return this - this.toInt() == 0.0
}

fun Double.round(n: Int): Double {
    return BigDecimal(this).setScale(n, RoundingMode.HALF_EVEN).toDouble()
}