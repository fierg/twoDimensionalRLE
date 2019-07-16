package edu.ba.twoDimensionalRLE.extensions

import java.util.*

fun ByteArray.toBitSetList(): List<BitSet> {
    var bitSetList = mutableListOf<BitSet>()
    for (byte in this) {
        bitSetList.add(byte.toBitSet())
    }
    return bitSetList
}
