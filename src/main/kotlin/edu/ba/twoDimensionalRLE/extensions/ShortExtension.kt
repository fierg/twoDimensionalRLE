package edu.ba.twoDimensionalRLE.extensions

public fun Short.shl(i: Int): Short {
    return this.toInt().shl(i).toShort()
}