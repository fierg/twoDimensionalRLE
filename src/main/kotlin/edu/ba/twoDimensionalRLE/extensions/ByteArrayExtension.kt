package edu.ba.twoDimensionalRLE.extensions

import java.util.*

fun ByteArray.toBitSetList(): List<BitSet> {
    val bitSetList = mutableListOf<BitSet>()
    for (byte in this) {
        bitSetList.add(byte.toBitSet())
    }
    return bitSetList
}

@ExperimentalUnsignedTypes
fun UByteArray.toBitSetList(): List<BitSet> {
    val bitSetList = mutableListOf<BitSet>()
    for (byte in this) {
        bitSetList.add(byte.toBitSet())
    }
    return bitSetList
}



