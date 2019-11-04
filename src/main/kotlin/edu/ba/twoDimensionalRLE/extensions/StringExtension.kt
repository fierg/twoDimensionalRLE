package edu.ba.twoDimensionalRLE.extensions

fun List<String>.reduceToSingleChar(): Char {
    return when {
        this.size == 2 -> this[0].toInt().shl(4).or(this[1].toInt()).toChar()
        this.size == 1 -> this[0].toInt().shl(4).toChar()
        else -> throw IllegalArgumentException()
    }
}