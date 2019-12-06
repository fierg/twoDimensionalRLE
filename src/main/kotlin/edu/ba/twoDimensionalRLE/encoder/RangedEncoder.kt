package edu.ba.twoDimensionalRLE.encoder

import edu.ba.twoDimensionalRLE.model.DataChunk
import loggersoft.kotlin.streams.BitStream

interface RangedEncoder : Encoder {
    fun encodeChunkBinRLE(chunk: DataChunk, range: IntRange, bitsPerNumber: Int, byteSize: Int): DataChunk
    fun decodeChunkBinRLE(
        chunk: DataChunk,
        range: IntRange,
        bitsPerNumber: Int,
        byteSize: Int,
        rleNumbers: List<Int>
    ): DataChunk

    fun decodeChunkHuffman(
        chunk: DataChunk,
        range: IntRange,
        byteSize: Int,
        stream: BitStream,
        huffmanMapping: Map<StringBuffer, Byte>,
        expectedHuffmanBytes: Int
    ): DataChunk

    fun encodeChunkHuffman(
        chunk: DataChunk,
        range: IntRange,
        bitsPerNumber: Int,
        byteSize: Int,
        mapping: Map<Byte, StringBuffer>
    ): DataChunk
}