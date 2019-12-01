package edu.ba.twoDimensionalRLE.encoder

import edu.ba.twoDimensionalRLE.model.DataChunk

interface RangedEncoder : Encoder {
    fun encodeChunk(chunk: DataChunk, range: IntRange, bitsPerNumber: Int, byteSize: Int): DataChunk
    fun decodeChunkRLE(
        chunk: DataChunk,
        range: IntRange,
        bitsPerNumber: Int,
        byteSize: Int,
        rleNumbers: List<Int>
    ): DataChunk

    fun decodeChunkHuffman(chunk: DataChunk, range: IntRange, byteSize: Int): DataChunk
}