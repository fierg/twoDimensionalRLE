package edu.ba.twoDimensionalRLE.encoder.mixed

import de.jupf.staticlog.Log
import de.jupf.staticlog.core.Logger
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.Encoder
import edu.ba.twoDimensionalRLE.encoder.huffman.HuffmanEncoder
import edu.ba.twoDimensionalRLE.encoder.rle.BinaryRunLengthEncoder
import edu.ba.twoDimensionalRLE.extensions.writeInvertedToBinaryStream
import edu.ba.twoDimensionalRLE.model.DataChunk
import edu.ba.twoDimensionalRLE.tranformation.BurrowsWheelerTransformation
import loggersoft.kotlin.streams.BitStream
import loggersoft.kotlin.streams.openBinaryStream
import loggersoft.kotlin.streams.toIntUnsigned
import java.io.File
import kotlin.math.ceil
import kotlin.math.log2

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
class MixedEncoder : Encoder {

    private val byteArraySize = 254
    private val bwt = BurrowsWheelerTransformation()
    private val analyzer = Analyzer()
    private val binaryRunLengthEncoder = BinaryRunLengthEncoder()
    private val DEBUG = true

    private val log = Log.kotlinInstance()

    companion object {
        private const val byteSize = 8
        private const val bitsPerRLENumber = 8
        val RLE_BIT_RANGE = IntRange(7, 7)
        val HUFF_BIT_RANGE = IntRange(0, 6)
    }

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    override fun encode(inputFile: String, outputFile: String) {
        encodeInternal(inputFile, outputFile)
    }

    override fun decode(inputFile: String, outputFile: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun encodeInternal(inputFile: String, outputFile: String) {
        analyzer.analyzeFile(File(inputFile))
        analyzer.addBWTSymbolsToMapping()
        val huffmanEncoder = HuffmanEncoder()

        val mapping = analyzer.getByteMapping()
        val chunks = DataChunk.readChunksFromFile(inputFile, byteArraySize, log)
        val transformedChunks = mutableListOf<DataChunk>()
        val mappedChunks = mutableListOf<DataChunk>()
        val encodedChunks = mutableListOf<DataChunk>()

        log.info("Performing burrows wheeler transformation on all chunks, adding 2 Byte...")
        chunks.forEach {
            transformedChunks.add(bwt.transformDataChunk(it))
        }
        log.info("Finished burrows wheeler transformation.")

        if (DEBUG) {
            transformedChunks.stream().forEach { it.appendCurrentChunkToFile(outputFile + "_transformed") }
        }

        log.info("Performing byte mapping to lower values on all chunks...")
        transformedChunks.forEach {
            mappedChunks.add(it.applyByteMapping(mapping))
        }
        log.info("Finished byte mapping.")

        if (DEBUG) {
            mappedChunks.stream().forEach { it.appendCurrentChunkToFile(outputFile + "_mapped") }
        }

        log.info("Encoding all chunks with binary RLE and Huffman Encoding in parallel...")
        log.info("Using run length encoding for line ${RLE_BIT_RANGE.first} until ${RLE_BIT_RANGE.last}")
        log.info("Using huffman encoding for line ${HUFF_BIT_RANGE.first} until ${HUFF_BIT_RANGE.last}")

        mappedChunks.forEach {
            var encodedChunk = binaryRunLengthEncoder.encodeChunk(it, RLE_BIT_RANGE, bitsPerRLENumber, byteSize)
            encodedChunk = huffmanEncoder.encodeChunk(encodedChunk, HUFF_BIT_RANGE, bitsPerRLENumber, byteSize)
            encodedChunks.add(encodedChunk)
        }
        log.info("Finished encoding.")

        log.info("Writing all encoded lines of all chunks to file...")
        encodedChunks.stream().forEach { it.writeEncodedLinesToFile(outputFile) }
        log.info("Finished encoding.")
    }


    fun readEncodedFileConsecutive(
        inputFile: String,
        byteArraySize: Int,
        log: Logger,
        rleRange: IntRange,
        huffmanRange: IntRange,
        huffmanMapping: Map<StringBuffer, Byte>
    ): List<DataChunk> {
        val input = File(inputFile)
        val chunks = mutableListOf<DataChunk>()
        val expectedRLEBits = byteArraySize * (rleRange.last + 1 - rleRange.first)
        val expectedHuffmanBytes = byteArraySize * (huffmanRange.last + 1 - huffmanRange.first)
        val rle = BinaryRunLengthEncoder()
        val huff = HuffmanEncoder()

        log.info("Reading $input consecutively into chunks of size $byteArraySize bytes...")

        debugPrintFileContent(input)

        BitStream(File(input.toURI()).openBinaryStream(false)).use { stream ->
            while (stream.position <= stream.size) {
                val currentChunk = DataChunk(ByteArray(0))

                val rleNumbers = mutableListOf<Int>()
                while (stream.bitPosition < stream.size * 8 && rleNumbers.sum() <= expectedRLEBits) {
                    rleNumbers.add(stream.readBits(bitsPerRLENumber, false).toInt())
                }

                rle.decodeChunkRLE(currentChunk, rleRange, bitsPerRLENumber, byteSize, rleNumbers)
                huff.decodeChunkHuffman(
                    currentChunk,
                    HUFF_BIT_RANGE,
                    bitsPerRLENumber,
                    stream,
                    huffmanMapping,
                    expectedHuffmanBytes
                )

                chunks.add(currentChunk)
            }
        }

        return chunks
    }

    fun debugPrintFileContent(input: File) {
        if (DEBUG) {
            BitStream(File(input.toURI()).openBinaryStream(false)).use { stream ->
                while (stream.bitPosition < stream.size * 8) {
                    print(if (stream.readBit()) "1" else "0")
                }
            }

            println()

            BitStream(File(input.toURI()).openBinaryStream(false)).use { stream ->
                while (stream.position < stream.size) {
                    print(stream.readByte().toChar())
                }
            }

            println()

            BitStream(File(input.toURI()).openBinaryStream(false)).use { stream ->
                while (stream.position < stream.size) {
                    print(stream.readByte().toIntUnsigned())
                    print(" ")
                }
            }
        }
    }

    fun writeByteMappingToStream(stream: BitStream, mapping: Map<Byte, Byte>) {
        writeByteMappingLengthToStream(mapping, stream)

        mapping.forEach { (input, _) ->
            stream.write(input)
        }
        stream.write(0.toByte())
    }

    private fun writeByteMappingLengthToStream(
        mapping: Map<Byte, Byte>,
        stream: BitStream
    ): Long {
        val bytesNeeded = ceil(log2(mapping.size.toDouble() + 1) / 8)

        log.info("Write mapping length header with $bytesNeeded bytes size.")

        var header = mapping.size.toString(2)
        val padSize = (bytesNeeded * 8).toInt()
        header = header.padStart(padSize, '0')

        StringBuffer(header).writeInvertedToBinaryStream(stream)
        stream.write(0.toByte())

        if (DEBUG) {
            stream.flush()
        }

        return (bytesNeeded).toLong() + 1
    }

}