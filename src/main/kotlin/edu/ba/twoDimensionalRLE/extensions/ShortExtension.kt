package edu.ba.twoDimensionalRLE.extensions

fun Short.shl(i: Int): Short {
    return this.toInt().shl(i).toShort()
}