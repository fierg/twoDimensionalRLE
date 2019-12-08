package edu.ba.twoDimensionalRLE.extensions

import loggersoft.kotlin.streams.BitStream
import java.util.*


@Deprecated("Use toByteSet!")
fun StringBuffer.toBitSet(): BitSet {
    require(this.toString().matches(Regex("([01]*)")))

    val result = BitSet(this.length)
    this.forEachIndexed { index, char ->
        if (char == "1"[0]) result.set(index, true)
        else if (char == "0"[0]) result.set(index, false)
    }
    return result
}

@Deprecated("Use toByteSet!")
fun StringBuffer.toBitSet(n :Int): BitSet {
    require(this.toString().matches(Regex("([01]*)")))

    val result = BitSet(n)
    this.forEachIndexed { index, char ->
        if (char == "1"[0]) result.set(index, true)
        else if (char == "0"[0]) result.set(index, false)
    }
    return result
}

fun StringBuffer.toByteArray(): ByteArray {
    require(this.toString().matches(Regex("([01]*)")))
    var result = ByteArray(0)

    this.chunked(8).forEach { chunk ->
        result += Integer.parseInt(chunk,2).toByte()
    }
    return result
}

@ExperimentalUnsignedTypes
fun StringBuffer.writeToBinaryStream(bitStream: BitStream) {
    this.forEach { char ->
        when (char) {
            '1' -> bitStream.write(true)
            '0' -> bitStream.write(false)
            else -> throw IllegalArgumentException("Unexpected buffer parsed!")
        }
    }
}

@ExperimentalUnsignedTypes
fun StringBuffer.writeInvertedToBinaryStream(bitStream: BitStream) {
    this.reverse().chunked(8).forEach { byte ->
        byte.forEach { bit ->
            when (bit) {
                '1' -> bitStream.write(true)
                '0' -> bitStream.write(false)
                else -> throw IllegalArgumentException("Unexpected buffer parsed!")
            }
        }
    }
}


