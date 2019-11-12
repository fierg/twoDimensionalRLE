package edu.ba.twoDimensionalRLE.extensions

import java.util.*

fun BitSet.toShiftedLeftByte(shiftCount: Int): Byte {
    require(this.length() < 9)
    return this.toByteArray()[0].toInt().shl(shiftCount).toByte()
}

fun BitSet.toShiftedRightByte(shiftCount: Int): Byte {
    require(this.length() < 9)
    return this.toByteArray()[0].toInt().shr(shiftCount).toByte()
}