package edu.ba.twoDimensionalRLE.encoder

import edu.ba.twoDimensionalRLE.model.DataChunk

interface RangedEncoder : Encoder {
    fun encodeChunk(chunk: DataChunk, range: IntRange, bitsPerNumber: Int, byteSize: Int): DataChunk
    fun decodeChunk(chunk: DataChunk, range: IntRange, bitsPerNumber: Int, byteSize: Int): DataChunk
}