package edu.ba.twoDimensionalRLE.extensions

import java.util.*
import kotlin.experimental.and
import kotlin.math.pow

fun Byte.toBitSet(): BitSet {
    val bitSet = BitSet(8)
    for (i in 0..7) {
        if (this and 2.toDouble().pow(i).toByte() == 2.toDouble().pow(i).toByte())
            bitSet.set(i)
    }
    if (this.coerceAtLeast(0) == 0.toByte()){
        bitSet.set(8)
    }
        return bitSet
}

@ExperimentalUnsignedTypes
fun UByte.toBitSet(): BitSet {
    val bitSet = BitSet(7)
    for (i in 0..7) {
        if (this and 2.toDouble().pow(i).toByte().toUByte() == 2.toDouble().pow(i).toByte().toUByte())
            bitSet.set(i)
    }
    return bitSet
}