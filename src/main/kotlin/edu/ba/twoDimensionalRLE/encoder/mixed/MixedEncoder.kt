package edu.ba.twoDimensionalRLE.encoder.mixed

import de.jupf.staticlog.Log
import de.jupf.staticlog.core.LogLevel
import de.jupf.staticlog.core.Logger
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.Encoder
import edu.ba.twoDimensionalRLE.encoder.huffman.HuffmanEncoder
import edu.ba.twoDimensionalRLE.encoder.rle.BinaryRunLengthEncoder
import edu.ba.twoDimensionalRLE.extensions.getSize
import edu.ba.twoDimensionalRLE.extensions.toByteArray
import edu.ba.twoDimensionalRLE.extensions.writeInvertedToBinaryStream
import edu.ba.twoDimensionalRLE.extensions.writeToBinaryStream
import edu.ba.twoDimensionalRLE.model.DataChunk
import edu.ba.twoDimensionalRLE.tranformation.BurrowsWheelerTransformation
import edu.ba.twoDimensionalRLE.tranformation.BurrowsWheelerTransformationModified
import loggersoft.kotlin.streams.BitStream
import loggersoft.kotlin.streams.openBinaryStream
import loggersoft.kotlin.streams.toIntUnsigned
import java.io.File
import kotlin.math.ceil
import kotlin.math.log2

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
class MixedEncoder : Encoder {

    private val bwt = BurrowsWheelerTransformation()
    private val bwtLinear = BurrowsWheelerTransformationModified()

    private val analyzer = Analyzer()
    private val binaryRunLengthEncoder = BinaryRunLengthEncoder()
    private val DEBUG = true

    private val log = Log.kotlinInstance()

    companion object {
        private const val byteSize = 8
        private const val bitsPerRLENumber = 4
        val BIN_RLE_BIT_RANGE = IntRange(0, 7)
        val HUFF_BIT_RANGE = IntRange(-1, -1)
        val RLE_RANGE = IntRange(-1, -1)
    }

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
        if (!DEBUG) log.logLevel = LogLevel.INFO

    }

    override fun encode(
        inputFile: String, outputFile: String, applyByteMapping: Boolean,
        applyBurrowsWheelerTransformation: Boolean , byteArraySize: Int
    ) {
        encodeInternal(inputFile, outputFile, applyByteMapping, applyBurrowsWheelerTransformation, byteArraySize)
    }

    override fun decode(
        inputFile: String,
        outputFile: String,
        applyByteMapping: Boolean,
        applyBurrowsWheelerTransformation: Boolean, byteArraySize: Int) {
        val input = File(inputFile)
        val huff = HuffmanEncoder()
        val binRLE = BinaryRunLengthEncoder()

        val totalByteArraySize = if (applyBurrowsWheelerTransformation) byteArraySize + 2 else byteArraySize
        log.info("Staring to decode $inputFile...")


        BitStream(input.openBinaryStream(true)).use { stream ->
            log.info("Trying to parse the total number of encoded bytes...")
            val totalSize = readHeaderFromEncodedFile(stream)

            log.info("Trying to parse the number of binary RLE encoded bytes...")
            val binRLElentgh = readHeaderFromEncodedFile(stream)

            //    log.info("Trying to parse nr of RLE encoded bytes...")
            //     val rlelentgh = readHeaderFromEncodedFile(stream)

            log.info("Trying to parse number of huffman encoded bytes...")
            val huffmanLength = readHeaderFromEncodedFile(stream)
            log.info("Expecting $binRLElentgh binary rle encoded numbers with size $bitsPerRLENumber bits, then $huffmanLength bytes of huffman encoded bytes.")

            val huffmanMappingSize = readHeaderFromEncodedFile(stream)
            val huffmanMapping = huff.parseHuffmanMappingFromStream(stream, huffmanMappingSize, log)

            //Go to the beginning of the next byte, huffman mapping can end in the middle of a byte
            if (stream.offset != 0) stream.position++

            log.debug("Trying to parse byte mapping size...")
            val byteMappingSize = readHeaderFromEncodedFile(stream)

            val mapping = readByteMappingFromStream(stream, byteMappingSize, log)
            val binRleNumbers = binRLE.readBinRLENumbersFromStream(stream, binRLElentgh, bitsPerRLENumber)
            val huffDecoded = huff.decodeFromStream(huffmanMapping, stream, huffmanLength)


            val decodedChunks = DataChunk.readChunksFromDecodedParts(
                totalSize,
                totalByteArraySize,
                BIN_RLE_BIT_RANGE,
                RLE_RANGE,
                HUFF_BIT_RANGE,
                huffDecoded.toByteArray(),
                binRLE.decodeBinRleNumbersToBuffer(binRleNumbers, bitsPerRLENumber),
                log
            )

            log.info("Applying reverted byte mapping to all chunks...")
            val remappedBytes = applyMapping(decodedChunks, mapping, outputFile)

          //  val result = mutableListOf<DataChunk>()
            //remappedBytes.forEach { result.add( bwt.invertTransformDataChunk(it))}

            log.info("Writing decoded result to $outputFile")
            remappedBytes.forEach { it.appendCurrentChunkToFile(outputFile) }
        }

    }

    private fun readHeaderFromEncodedFile(stream: BitStream): Int {
        return parseCurrentHeader(stream, 2, log)
    }

    private fun encodeInternal(
        inputFile: String,
        outputFile: String,
        applyByteMapping: Boolean,
        applyBurrowsWheelerTransformation: Boolean, byteArraySize: Int
    ) {
        val input = File(inputFile)
        var totalSize = input.length()
        val huffmanEncoder = HuffmanEncoder()
        var chunks = DataChunk.readChunksFromFile(inputFile, byteArraySize, log)
        val huffBuffer = StringBuffer()

        analyzer.analyzeFile(input)
        analyzer.addBWTSymbolsToMapping()

        if (DEBUG) {
            chunks.forEach {  it.debugPrintBinLines(outputFile + "_in_lines")}
        }

        if (applyBurrowsWheelerTransformation){
            chunks = bwt.performBurrowsWheelerTransformationOnAllChunks(chunks, outputFile)
            totalSize += (chunks.count() * 2)
            if (DEBUG) {
                chunks.forEach {  it.debugPrintBinLines(outputFile + "_in_lines_bwt")}
            }
        }

        if (applyByteMapping) {
            chunks = applyMapping(chunks, analyzer.getByteMapping(), outputFile)
            if (DEBUG) {
                chunks.forEach {  it.debugPrintBinLines(outputFile + "_in_lines_mapped")}
            }
        }

        printEncodingInfo()

        log.info("Collecting all bytes to encode with huffman encoding to build dictionary...")
        //TODO improve buffer collecting
        chunks.forEach {
            for (i in HUFF_BIT_RANGE) {
                huffBuffer.append(it.getLineFromChunkAsStringBuffer(i))
            }
        }

        log.debug("Creating byte array from ${huffBuffer.length} bit large buffer...")
        //TODO create better mapping creation to use less ram
        val huffBufferArray = huffBuffer.toByteArray()
        val huffmanMapping = huffmanEncoder.getHuffmanMapping(256, huffBufferArray)


        BitStream(File(outputFile).openBinaryStream(false)).use { stream ->
            val encodedChunks = encodeAllChunks(chunks, huffmanEncoder, huffmanMapping, huffBufferArray)

            val encodedBinRLENumbers = encodedChunks.map { it.binRleEncodedNumbers.count() }.sum()
            val encodedHuffBytes = encodedChunks.map { it.huffEncodedBytes }.sum()

            writeHeadersToStream(
                stream,
                huffmanEncoder,
                huffmanMapping,
                log,
                encodedBinRLENumbers,
                encodedHuffBytes,
                RLE_RANGE.getSize(),
                huffmanEncoder,
                analyzer.getByteMapping(),
                totalSize
            )


            if (DEBUG) {
                log.debug("Writing all encoded lines of all chunks to file ${outputFile + "_lines"}...")
                encodedChunks.stream().forEach { it.writeEncodedLinesToFile(outputFile + "_lines") }
            }

            appendEncodedChunksToStream(encodedChunks, stream)
        }
        log.info("Finished encoding.")
    }

    private fun appendEncodedChunksToStream(
        encodedChunks: MutableList<DataChunk>,
        stream: BitStream
    ) {
        log.debug("Appending all binary rle encoded numbers to output stream...")
        var currentOut = StringBuffer()
        encodedChunks.forEach { chunk ->
            currentOut.append(
                binaryRunLengthEncoder.encodeToBinaryStringBuffer(
                    chunk.binRleEncodedNumbers,
                    bitsPerRLENumber
                )
            )
        }
        currentOut.writeInvertedToBinaryStream(stream)

        log.debug("Wrote ${encodedChunks.map { it.binRleEncodedNumbers.count() }.sum()} binary rle encoded numbers to stream.")

        if (DEBUG) stream.flush()

        //TODO add regular rle
        /*
            log.debug("Appending all rle encoded bytes to stream...")
            currentOut = StringBuffer()
            encodedChunks.forEach { chunk ->
                currentOut.append(writeRLEencodedToFile(stream, chunk))
            }
            currentOut.toBitSet().toByteArray().forEach { stream.write(it) }
                        if (DEBUG) stream.flush()
            */

        log.debug("Appending all huffman encoded bytes to stream...")
        currentOut = StringBuffer()
        encodedChunks.forEach { chunk ->
            currentOut.append(chunk.huffEncodedStringBuffer)
        }
        currentOut.writeToBinaryStream(stream)
        if (DEBUG) stream.flush()
        log.debug("Appended all compressed content to encoded file.")
    }

    private fun encodeAllChunks(
        mappedChunks: MutableList<DataChunk>,
        huffmanEncoder: HuffmanEncoder,
        huffmanMapping: MutableMap<Byte, StringBuffer>,
        huffBufferArray: ByteArray
    ): MutableList<DataChunk> {
        log.info("Encoding all chunks...")
        val encodedChunks = mutableListOf<DataChunk>()
        mappedChunks.forEach {
            val encodedChunk =
                binaryRunLengthEncoder.encodeChunkBinRLE(it, BIN_RLE_BIT_RANGE, bitsPerRLENumber, byteSize)

            encodedChunks.add(encodedChunk)
        }
        encodedChunks[0] = huffmanEncoder.encodeAllChunksHuffman(
            encodedChunks.first(),
            huffmanMapping,
            huffBufferArray
        )
        return encodedChunks
    }

    private fun applyMapping(
        transformedChunks: List<DataChunk>,
        mapping: Map<Byte, Byte>,
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

    private fun writeHeadersToStream(
        stream: BitStream,
        encoder: Encoder,
        huffmanMapping: Map<Byte, StringBuffer>,
        log: Logger,
        binRLEsize: Int,
        huffSize: Int,
        rleSize: Int,
        huffmanEncoder: HuffmanEncoder,
        byteMapping: MutableMap<Byte, Byte>,
        totalFileSize: Long
    ) {
        // TODO: only write headders if value is provided
        log.info("Writing headers to file...")
        //TODO check if counter contains 0x00 and if so -> increase numberOfZerosAfter!
        val numbersOfZerosAfter = 2

        //TODO use long to enable encoding of files larger than 2147.483647 mb
        assert(totalFileSize < Int.MAX_VALUE)
        val totalFileSizeI = totalFileSize.toInt()
        log.debug(
            "Writing total size in bytes header, ${totalFileSizeI.toString(2).padStart(
                8,
                '0'
            )} , 0x${Integer.toHexString(
                totalFileSizeI
            )}"
        )
        encoder.writeLengthHeaderToFile(totalFileSizeI, stream, log, numbersOfZerosAfter)


        log.debug(
            "Writing binary rle header, ${binRLEsize.toString(2).padStart(8, '0')} , 0x${Integer.toHexString(
                binRLEsize
            )}"
        )
        encoder.writeLengthHeaderToFile(binRLEsize, stream, log, numbersOfZerosAfter)

        //     log.debug("Writing rle header, ${rleSize.toString(2)} , 0x${Integer.toHexString(rleSize)}")
        //    encoder.writeLengthHeaderToFile(rleSize, stream, log, numberOfZerosAfter)

        log.debug(
            "Writing huffman header, ${huffSize.toString(2).padStart(
                8,
                '0'
            )} , 0x${Integer.toHexString(huffSize)}"
        )
        encoder.writeLengthHeaderToFile(huffSize, stream, log, numbersOfZerosAfter)

        log.debug(
            "Writing huffman mapping length to stream, ${huffmanMapping.size.toString(2).padStart(
                8,
                '0'
            )} , 0x${Integer.toHexString(
                huffmanMapping.size
            )}"
        )
        encoder.writeLengthHeaderToFile(huffmanMapping.size, stream, log, numbersOfZerosAfter)

        log.debug("Writing huffman mapping to file ${huffmanMapping}...")
        huffmanEncoder.writeDictionaryToStream(stream, huffmanMapping)

        log.debug(
            "Writing mapping size header ${byteMapping.size.toString(2).padStart(
                8,
                '0'
            )} , 0x${Integer.toHexString(byteMapping.size)}"
        )
        encoder.writeLengthHeaderToFile(byteMapping.size, stream, log, numbersOfZerosAfter)

        log.debug("Writing byteMapping to stream ${byteMapping.map { entry -> "0x" + Integer.toHexString(entry.key.toInt()) }}")
        encoder.writeByteMappingToStream(stream, byteMapping, log)

        log.debug("Finished writing header.")
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

                rle.decodeChunkBinRLE(currentChunk, rleRange, bitsPerRLENumber, byteSize, rleNumbers, byteArraySize)
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
            log.debug("Printing as binary string...")
            BitStream(File(input.toURI()).openBinaryStream(false)).use { stream ->
                while (stream.bitPosition < stream.size * 8) {
                    print(if (stream.readBit()) "1" else "0")
                }
            }

            println()

            log.debug("Printing as chars...")
            BitStream(File(input.toURI()).openBinaryStream(false)).use { stream ->
                while (stream.position < stream.size) {
                    print(stream.readByte().toChar())
                }
            }

            println()

            log.debug("Printing as unsigned byte values...")
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