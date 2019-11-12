package edu.ba.twoDimensionalRLE.encoder

import edu.ba.twoDimensionalRLE.model.DataChunk

interface RangedEncoder : Encoder {
    fun encodeChunk(chunk: DataChunk, range: IntRange): DataChunk
    fun decodeChunk(chunk: DataChunk, range: IntRange): DataChunk
}