package edu.ba.twoDimensionalRLE.extensions

import kotlin.math.pow

fun Int.pow(i: Int): Int {
    val result = this.toDouble().pow(i)
    require(result <= Int.MAX_VALUE)
    return result.toInt()
}