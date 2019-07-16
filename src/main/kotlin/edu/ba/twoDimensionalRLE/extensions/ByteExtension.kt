package edu.ba.twoDimensionalRLE.extensions

import java.util.*
import kotlin.experimental.and
import kotlin.math.pow

fun Byte.toBitSet(): BitSet {
    val bitSet = BitSet(7)
    for (i in 0..7) {
        if (this and 2.toDouble().pow(i).toByte() == 2.toDouble().pow(i).toByte())
            bitSet.set(i)
    }
    return bitSet
}