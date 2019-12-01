package edu.ba.twoDimensionalRLE.extensions

import loggersoft.kotlin.streams.BitStream

@ExperimentalUnsignedTypes
fun BitStream.popNextNBitAsStringBuffer(n : Int): StringBuffer {
    val result = StringBuffer()
    val currentBitPosition = this.bitPosition

    for (i in 0 until n){
        result.append(if (this.readBit()) '1' else '0')
    }
    //resetting to old stream position
    this.bitPosition = currentBitPosition
    return result
}