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

@ExperimentalUnsignedTypes
fun ByteArray.toBinStringBuffer(): StringBuffer {
    val sb = StringBuffer()
     this.map { it.toUByte().toString(2).padStart(8, '0') }.forEach { sb.append(it) }
    return sb
}



