package edu.ba.twoDimensionalRLE.encoder.mixed

import de.jupf.staticlog.Log
import de.jupf.staticlog.core.Logger
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.Encoder
import edu.ba.twoDimensionalRLE.encoder.huffman.HuffmanEncoder
import edu.ba.twoDimensionalRLE.encoder.rle.BinaryRunLengthEncoder
import edu.ba.twoDimensionalRLE.extensions.getSize
import edu.ba.twoDimensionalRLE.extensions.toBitSet
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
    private val byteArraySizeAfterBWT = 256 / 8
    private val bwt = BurrowsWheelerTransformation()
    private val analyzer = Analyzer()
    private val binaryRunLengthEncoder = BinaryRunLengthEncoder()
    private val DEBUG = true

    private val log = Log.kotlinInstance()

    companion object {
        private const val byteSize = 8
        private const val bitsPerRLENumber = 4
        val BIN_RLE_BIT_RANGE = IntRange(3, 7)
        val HUFF_BIT_RANGE = IntRange(0, 2)
        val RLE_RANGE = IntRange(-1, -1)
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
        val input = File(inputFile)
        log.info("Staring to decode $inputFile...")


        BitStream(input.openBinaryStream(true)).use { stream ->
            val binRLElentgh = readHeaderFromEncodedFile(stream)
            val rlelentgh = readHeaderFromEncodedFile(stream)
            val huffmanlentgh = readHeaderFromEncodedFile(stream)

        }

    }

    private fun readHeaderFromEncodedFile(stream: BitStream): Int {
        return parseCurrentHeader(stream, 1, log)
    }

    private fun encodeInternal(inputFile: String, outputFile: String) {
        val input = File(inputFile)
        val huffmanEncoder = HuffmanEncoder()
        val chunks = DataChunk.readChunksFromFile(inputFile, byteArraySize, log)
        var huffbytes = ByteArray(0)

        analyzer.analyzeFile(input)
        analyzer.addBWTSymbolsToMapping()

        val transformedChunks = bwt.performBurrowsWheelerTransformationOnAllChunks(chunks, outputFile)
        val mappedChunks = applyMapping(transformedChunks, analyzer.getByteMapping(), outputFile)

        printEncodingInfo()

        log.info("Collecting all bytes to encode with huffman encoding to build dictionary...")
        mappedChunks.forEach {
            for (i in HUFF_BIT_RANGE) {
                huffbytes += it.getLineFromChunk(i, byteSize)
            }
        }

        //TODO create better mapping creation to use less space
        val huffmanMapping = huffmanEncoder.getHuffmanMapping(256, huffbytes)


        //TODO fix count for real.
        writeHeadersToEncodedFile(
            outputFile, huffmanEncoder, huffmanMapping, log, BIN_RLE_BIT_RANGE.getSize() * byteArraySizeAfterBWT,
            RLE_RANGE.getSize() * byteArraySizeAfterBWT, HUFF_BIT_RANGE.getSize() * byteArraySizeAfterBWT
        )

        val encodedChunks = encodeAllChunks(mappedChunks, huffmanEncoder, huffmanMapping)

        if (DEBUG) {
            log.debug("Writing all encoded lines of all chunks to file...")
            encodedChunks.stream().forEach { it.writeEncodedLinesToFile(outputFile + "_lines") }
        }

        BitStream(File(outputFile).openBinaryStream(false)).use { stream ->

            log.debug("Appending all binary rle encoded numbers to stream...")
            var currentOut = StringBuffer()
            encodedChunks.forEach { chunk ->
                currentOut.append(binaryRunLengthEncoder.encodeToBinaryStringBuffer(chunk.binRleEncodedNumbers, bitsPerRLENumber))
            }
            currentOut.toBitSet().toByteArray().forEach { stream.write(it) }

            if(DEBUG) stream.flush()

            //TODO
            /*
                        log.debug("Appending all rle encoded bytes to stream...")
            currentOut = StringBuffer()
            encodedChunks.forEach { chunk ->
                currentOut.append(writeRLEencodedToFile(stream, chunk))
            }
                        currentOut.toBitSet().toByteArray().forEach { stream.write(it) }

            */

            log.debug("Appending all huffman encoded bytes to stream...")
            currentOut = StringBuffer()
            encodedChunks.forEach { chunk ->
                currentOut.append(chunk.huffEncodedStringBuffer) }
            currentOut.toBitSet().toByteArray().forEach { stream.write(it) }

        }
        log.info("Finished encoding.")
    }

    private fun encodeAllChunks(
        mappedChunks: MutableList<DataChunk>,
        huffmanEncoder: HuffmanEncoder,
        huffmanMapping: MutableMap<Byte, StringBuffer>
    ): MutableList<DataChunk> {
        log.info("Encoding all chunks...")
        val encodedChunks = mutableListOf<DataChunk>()
        mappedChunks.forEach {
            var encodedChunk =
                binaryRunLengthEncoder.encodeChunkBinRLE(it, BIN_RLE_BIT_RANGE, bitsPerRLENumber, byteSize)
            encodedChunk = huffmanEncoder.encodeChunkHuffman(
                encodedChunk,
                HUFF_BIT_RANGE,
                bitsPerRLENumber,
                byteSize,
                huffmanMapping
            )
            encodedChunks.add(encodedChunk)
        }
        log.info("Finished encoding.")
        return encodedChunks
    }

    private fun applyMapping(
        transformedChunks: MutableList<DataChunk>,
        mapping: MutableMap<Byte, Byte>,
        outputFile: String
    ): MutableList<DataChunk> {
        val mappedChunks = mutableListOf<DataChunk>()
        log.info("Performing byte mapping to lower values on all chunks...")
        transformedChunks.forEach {
            mappedChunks.add(it.applyByteMapping(mapping))
        }
        log.info("Finished byte mapping.")

        if (DEBUG) {
            mappedChunks.stream().forEach { it.appendCurrentChunkToFile(outputFile + "_mapped") }
        }
        return mappedChunks
    }

    private fun writeHeadersToEncodedFile(
        outputFile: String,
        encoder: Encoder,
        huffmanMapping: Map<Byte, StringBuffer>,
        log: Logger,
        binRLEsize: Int,
        huffSize: Int,
        rleSize: Int
    ) {
        log.info("Writing headers to file...")

        BitStream(File(outputFile).openBinaryStream(false)).use { stream ->
            //TODO check if counter contains 0x00 and if so -> increase numberOfZerosAfter!
            log.debug("Writing binary rle header, ${binRLEsize.toString(2)} , ${Integer.toHexString(binRLEsize)}")
            encoder.writeLengthHeaderToFile(binRLEsize, stream, log, 1)

            log.debug("Writing rle header, ${rleSize.toString(2)} , ${Integer.toHexString(rleSize)}")
            encoder.writeLengthHeaderToFile(rleSize, stream, log, 1)

            log.debug("Writing huffman header, ${huffSize.toString(2)} , ${Integer.toHexString(huffSize)}")
            encoder.writeLengthHeaderToFile(huffSize, stream, log, 1)

            log.debug(
                "Writing huffman mapping length to file, ${huffmanMapping.size.toString(2)} , ${Integer.toHexString(
                    huffmanMapping.size
                )}"
            )
            encoder.writeHuffmanMappingLengthToFile(huffmanMapping, stream, log)
        }
    }

    private fun printEncodingInfo() {
        log.info("Encoding all chunks with binary RLE and Huffman Encoding in parallel...")
        if (BIN_RLE_BIT_RANGE.getSize() > 0)
            log.info("Using binary run length encoding for line ${BIN_RLE_BIT_RANGE.first} until ${BIN_RLE_BIT_RANGE.last}")
        if (HUFF_BIT_RANGE.getSize() > 0)
            log.info("Using huffman encoding for line ${HUFF_BIT_RANGE.first} until ${HUFF_BIT_RANGE.last}")
        if (RLE_RANGE.getSize() > 0)
            log.info("Using huffman encoding for line ${RLE_RANGE.first} until ${RLE_RANGE.last}")
    }


    @Deprecated("use decode")
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

                rle.decodeChunkBinRLE(currentChunk, rleRange, bitsPerRLENumber, byteSize, rleNumbers)
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
            log.info("Printing as binary string...")
            BitStream(File(input.toURI()).openBinaryStream(false)).use { stream ->
                while (stream.bitPosition < stream.size * 8) {
                    print(if (stream.readBit()) "1" else "0")
                }
            }

            println()

            log.info("Printing as chars...")
            BitStream(File(input.toURI()).openBinaryStream(false)).use { stream ->
                while (stream.position < stream.size) {
                    print(stream.readByte().toChar())
                }
            }

            println()

            log.info("Printing as unsigned byte values...")
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