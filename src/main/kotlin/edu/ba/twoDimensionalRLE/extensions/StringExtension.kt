package edu.ba.twoDimensionalRLE.extensions

import java.util.*

fun List<String>.reduceToSingleChar(): Char {
    return when (this.size) {
        2 -> this[0].toInt().shl(4).or(this[1].toInt()).toChar()
        1 -> this[0].toInt().shl(4).toChar()
        else -> throw IllegalArgumentException()
    }
}

fun String.toBytePair(): Pair<BitSet, Int> {
    require(this.matches(Regex("([01]+)")))

    val result = BitSet()
    var count = 0
    this.forEachIndexed { index, char ->
        if (char == "1"[0]) result.set(index, true)
        count++
    }
    return Pair(result, count)
}