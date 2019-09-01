package edu.ba.twoDimensionalRLE.extensions

fun List<String>.reduceToSingleChar(): Char {
    if (this.size == 2) return this[0].toInt().shl(4).or(this[1].toInt()).toChar()
    else if (this.size == 1) return this[0].toInt().shl(4).toChar()
    else throw IllegalArgumentException()
}