package edu.ba.twoDimensionalRLE.extensions

import loggersoft.kotlin.streams.BitStream
import java.util.*

fun StringBuffer.toBitSet(): BitSet {
    if (this.isBlank()) return BitSet()
    require(this.toString().matches(Regex("([01]+)")))

    val result = BitSet()
    this.forEachIndexed { index, char ->
        if (char == "1"[0]) result.set(index, true)
        else if (char == "0"[0]) result.set(index, false)
    }
    return result
}

@ExperimentalUnsignedTypes
fun StringBuffer.writeToBinaryStream(bitStream: BitStream) {
    require(this.toString().matches(Regex("([01]+)")))
    this.forEach { char ->
        when (char) {
            '1' -> bitStream.write(true)
            '0' -> bitStream.write(false)
            else -> throw IllegalArgumentException("Unexpected buffer parsed!")
        }
    }
}

