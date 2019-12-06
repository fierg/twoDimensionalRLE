package edu.ba.twoDimensionalRLE.encoder.huffman


import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.encoder.Encoder
import edu.ba.twoDimensionalRLE.encoder.RangedEncoder
import edu.ba.twoDimensionalRLE.extensions.*
import edu.ba.twoDimensionalRLE.model.DataChunk
import loggersoft.kotlin.streams.BitStream
import loggersoft.kotlin.streams.openBinaryStream
import java.io.File
import java.util.*
import kotlin.experimental.or

@ExperimentalUnsignedTypes
class HuffmanEncoder : Encoder, RangedEncoder {
    private val log = Log.kotlinInstance()
    val mapping = mutableMapOf<Byte, StringBuffer>()
    private val byteArraySize = 256
    private val byteSize = 8
    private val DEBUG = true

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    override fun encodeChunkBinRLE(chunk: DataChunk, range: IntRange, bitsPerNumber: Int, byteSize: Int): DataChunk {
        throw NotImplementedError()
    }

    override fun decodeChunkBinRLE(
        chunk: DataChunk,
        range: IntRange,
        bitsPerNumber: Int,
        byteSize: Int,
        rleNumbers: List<Int>
    ): DataChunk {
        throw NotImplementedError()
    }

    override fun encode(inputFile: String, outputFile: String) {
        log.info("Parsing input file $inputFile ...")
        val bytes = File(inputFile).readBytes()
        val count = File(inputFile).length()
        log.info("Parsed $count bytes. Writing to encoded File as expected length.")

        log.info("Building tree to generate Huffman mapping...")
        val huffmanMapping = getHuffmanMapping(bytes.size, bytes)
        log.info("Finished building tree.")

        BitStream(File(outputFile).openBinaryStream(false)).use { stream ->
            val headerSizePosition = writeDecodedLengthHeaderToFile(count, stream, log)
            stream.position = headerSizePosition

            val mappingSizePosition = writeHuffmanMappingLengthToFile(huffmanMapping, stream, log)
            stream.position = headerSizePosition + mappingSizePosition

            writeDictionaryToStream(stream, huffmanMapping)

            log.info("Writing mapped input file as binary stream to $outputFile")
            bytes.forEach { byte ->
                huffmanMapping[byte]?.writeToBinaryStream(stream)
            }
        }
        log.info("Finished huffman encoding.")
    }

    override fun encodeChunkHuffman(
        chunk: DataChunk,
        range: IntRange,
        bitsPerNumber: Int,
        byteSize: Int,
        mapping: Map<Byte, StringBuffer>
    ): DataChunk {
        var linesToEncode = ByteArray(0)

        for (index in range) {
            assert(index in 0 until byteSize)
            linesToEncode += chunk.getLineFromChunk(index, bitSize = 8)
        }
        val encodedLines = writeEncodedAsStringBuffer(linesToEncode, mapping)
        chunk.huffEncodedStringBuffer = encodedLines
        if(DEBUG) chunk.encodedLines[range.last] = encodedLines.toBitSet().toByteArray()
        return chunk
    }

    override fun decodeChunkHuffman(
        chunk: DataChunk,
        range: IntRange,
        byteSize: Int,
        stream: BitStream,
        huffmanMapping: Map<StringBuffer, Byte>,
        expectedHuffmanBytes: Int
    ): DataChunk {

        require(huffmanMapping.isNotEmpty())

        val smallestMapping = huffmanMapping.keys.minBy { it.length }!!.length
        val largesMapping = huffmanMapping.keys.maxBy { it.length }!!.length
        var currentMappingSize = smallestMapping
        var currentPrefixSeen: StringBuffer
        val decodingResult = mutableListOf<Byte>()
        val stringMapping =
            huffmanMapping.map { entry: Map.Entry<StringBuffer, Byte> -> entry.key.toString() to entry.value }.toMap()

        while ((stream.bitPosition + currentMappingSize) < stream.size * 8 && decodingResult.size <= expectedHuffmanBytes) {
            currentPrefixSeen = stream.popNextNBitAsStringBuffer(currentMappingSize)

            if (stringMapping.containsKey(currentPrefixSeen.toString())) {
                decodingResult.add(stringMapping.getOrElse(currentPrefixSeen.toString()) { throw IllegalStateException() })
                stream.bitPosition = stream.bitPosition + currentMappingSize
                currentMappingSize = smallestMapping
            } else {
                currentMappingSize++
                assert(currentMappingSize <= largesMapping)
            }
        }
        return chunk
    }

    override fun decode(inputFile: String, outputFile: String) {
        log.info("Starting to decode huffman encoding from $inputFile ...")
        var decodingResult = ByteArray(0)

        BitStream(File(inputFile).openBinaryStream(true)).use { stream ->
            val expectedSize = parseLengthHeader(stream, log)
            val expectedMappingSize = parseMappingSizeHeader(stream)
            val huffmanMapping = parseHuffmanMapping(stream, expectedMappingSize)

            decodingResult = decodeFileInternal(huffmanMapping, stream, expectedSize).toByteArray()
        }

        log.info("Finished decoding. Writing decoded stream to $outputFile ...")
        File(outputFile).writeBytes(decodingResult)
        log.info("Finished decoding.")
    }

    private fun parseMappingSizeHeader(stream: BitStream): Long {
        var skip = true
        var skippedBytes = 0L
        stream.position = 0
        while (stream.position < stream.size && skip) {
            if (stream.readByte() == 0.toByte()) skip = false
            skippedBytes++
        }

        log.info("Trying to parse mapping size header...")
        var currentByteSize = 0
        var expectedSize = 0L

        while (expectedSize == 0L) {
            val currentByte = stream.readByte()
            if (currentByte != 0.toByte()) {
                currentByteSize++
            } else {
                stream.position = skippedBytes
                expectedSize = stream.readBits(currentByteSize * 8, true)
            }
        }
        log.info("Expecting $expectedSize mappings of <byte, Pair <length , prefix>> in encoded file.")
        return expectedSize
    }

    private fun parseHuffmanMapping(stream: BitStream, expectedMappingSize: Long): Map<StringBuffer, Byte> {

        log.info("Trying to parse $expectedMappingSize mappings from encoded file...")
        var skipContentLengthHeader = true
        var skipMappingSizeHeader = true

        stream.position = 0
        while (stream.position < stream.size && (skipContentLengthHeader || skipMappingSizeHeader)) {
            if (!skipContentLengthHeader && stream.readByte() == 0.toByte()) skipMappingSizeHeader = false
            if (skipContentLengthHeader && stream.readByte() == 0.toByte()) skipContentLengthHeader = false
        }

        val huffmanMapping = mutableMapOf<StringBuffer, Byte>()

        while (stream.position < stream.size && huffmanMapping.size < expectedMappingSize) {
            val currentPrefix = StringBuffer()
            val byteToMap = stream.readByte()
            val prefixLength = stream.readBits(8, false)
            for (position in 0 until prefixLength) {
                if (stream.readBit()) currentPrefix.append('1') else currentPrefix.append('0')
            }
            huffmanMapping[currentPrefix] = byteToMap
        }
        log.info("Parsed ${huffmanMapping.size} mappings from encoded file.")
        return huffmanMapping
    }

    private fun decodeFileInternal(
        huffmanMapping: Map<StringBuffer, Byte>,
        stream: BitStream,
        expectedSize: Long
    ): MutableList<Byte> {
        val smallestMapping = huffmanMapping.keys.minBy { it.length }!!.length
        val largesMapping = huffmanMapping.keys.maxBy { it.length }!!.length
        var currentMappingSize = smallestMapping
        var currentPrefixSeen: StringBuffer
        val decodingResult = mutableListOf<Byte>()
        val stringMapping =
            huffmanMapping.map { entry: Map.Entry<StringBuffer, Byte> -> entry.key.toString() to entry.value }.toMap()

        stream.position++

        log.info("Parsing encoded file with mapping $stringMapping")
        while ((stream.bitPosition + currentMappingSize) < stream.size * 8 && decodingResult.size < expectedSize) {
            currentPrefixSeen = stream.popNextNBitAsStringBuffer(currentMappingSize)

            if (stringMapping.containsKey(currentPrefixSeen.toString())) {
                decodingResult.add(stringMapping.getOrElse(currentPrefixSeen.toString()) { throw IllegalStateException() })
                stream.bitPosition = stream.bitPosition + currentMappingSize
                currentMappingSize = smallestMapping
            } else {
                currentMappingSize++
                assert(currentMappingSize <= largesMapping)
            }
        }
        if (decodingResult.size != expectedSize.toInt()) {
            log.warn("Decoded result has unexpected size!")
            log.warn("decoded ${decodingResult.size} bytes but expected $expectedSize bytes")
        }
        return decodingResult
    }


    fun decode(encodedByteArray: ByteArray, dictEncode: Map<Byte, StringBuffer>): ByteArray {
        val dictDecode = dictEncode.entries.asSequence().associateBy({ it.value }) { it.key }
        val offset = encodedByteArray.first().toInt()
        val byteString =
            encodedByteArray.asSequence().drop(1).map { byteToString(it) }.joinToString("").dropLast(offset)
        return decodeWithLoop(dictDecode, byteString)
    }

     fun writeDictionaryToStream(stream: BitStream, huffmanMapping: Map<Byte, StringBuffer>) {
        log.info("Writing huffman dictionary to file...")

        huffmanMapping.forEach { (byte, mapping) ->
            stream.write(byte)
            stream.write(getLengthOfMapping(mapping))
            mapping.writeToBinaryStream(stream)
        }
        if (DEBUG) {
            stream.flush()
        }
        log.info("Finished writing dictionary.")
    }

    private fun getLengthOfMapping(mapping: StringBuffer): Byte {
        return mapping.length.toByte()
    }

    private fun byteToString(byte: Byte): String {
        return ("0000000" + byte.toString(2)).substring(byte.toString(2).length)
    }

    private fun decodeWithLoop(dictDecode: Map<StringBuffer, Byte>, byteString: String): ByteArray {
        var decryptedIndex = 0
        var possibleIndex = 0
        val decrypted = mutableListOf<Byte>()
        while (decryptedIndex < byteString.length) {
            possibleIndex += 1
            val possibleByte = StringBuffer(byteString.substring(decryptedIndex, possibleIndex))
            val currentByte = dictDecode[possibleByte]
            if (currentByte != null) {
                decrypted.add(currentByte)
                decryptedIndex = possibleIndex
            }
        }
        return decrypted.toByteArray()
    }

    fun getHuffmanMapping(noOfChars: Int, byteArray: ByteArray): MutableMap<Byte, StringBuffer> {
        log.info("Generating Huffman Dictionary to encode file...")

        val huffmanTree = buildTree(countOccurrences(byteArray, noOfChars))
        return getMappingFromTree(huffmanTree, StringBuffer())
    }

    private fun writeEncodedAsByteArray(bytes: ByteArray, mapping: Map<Byte, StringBuffer>): ByteArray {
        val result = StringBuffer()
        bytes.forEach {
            result.append(mapping[it])
        }
        return result.toBitSet().toByteArray()
    }

    private fun writeEncodedAsStringBuffer(bytes: ByteArray, mapping: Map<Byte, StringBuffer>): StringBuffer {
        val result = StringBuffer()
        bytes.forEach {
            result.append(mapping[it])
        }
        return result
    }

    private fun writeEncoded(bytes: ByteArray, huffmanMapping: MutableMap<Byte, String>) {
        var currentBitPosition = 0
        var currentBytePosition = 0

        val result = ByteArray(32)

        bytes.forEach { byte ->
            val mappedByte = huffmanMapping[byte]!!.toBytePair()

            var currentByteVal = result.getOrElse(currentBytePosition) { 0.toByte() }
            val orderPosition = byteSize - 1 - (currentBitPosition % byteSize)
            val gapSize = orderPosition - (currentBitPosition % byteSize)


            if (gapSize >= mappedByte.second) {
                // huffman code fits into current byte

                val shiftSize = orderPosition + 1 - mappedByte.second
                currentByteVal = currentByteVal.or(mappedByte.first.toShiftedLeftByte(shiftSize))
                result[currentBytePosition] = currentByteVal

                currentBitPosition += mappedByte.second
                currentBytePosition = currentBitPosition / byteSize
            } else {
                //it doesn't
                val secondHalfSize = mappedByte.second - gapSize

                currentByteVal = currentByteVal.or(mappedByte.first.toShiftedRightByte(secondHalfSize))
                result[currentBytePosition] = currentByteVal

                currentBitPosition += gapSize
                currentBytePosition = currentBitPosition / byteSize
                val shiftSize = byteSize - secondHalfSize

                currentByteVal = result.getOrElse(currentBytePosition) { 0.toByte() }
                currentByteVal = currentByteVal.or(mappedByte.first.toShiftedLeftByte(shiftSize))
                result[currentBytePosition] = currentByteVal

                currentBitPosition += secondHalfSize
                currentBytePosition = currentBitPosition / byteSize
            }
        }
    }

    private fun buildTree(byteFrequencies: IntArray): HuffmanTree {
        val trees = PriorityQueue<HuffmanTree>()

        byteFrequencies.forEachIndexed { index, freq ->
            if (freq > 0) trees.offer(HuffmanLeaf(freq, index.toByte()))
        }

        assert(trees.size > 0)
        while (trees.size > 1) {
            val a = trees.poll()
            val b = trees.poll()
            trees.offer(HuffmanNode(a, b))
        }

        return trees.poll()
    }

    private fun countOccurrences(bytes: ByteArray, maxSymbols: Int): IntArray {
        val frequencies = IntArray(maxSymbols)
        bytes.forEach { byte -> frequencies[byte.index()] = frequencies[byte.index()] + 1 }
        return frequencies
    }

    private fun getMappingFromTree(tree: HuffmanTree, prefix: StringBuffer): MutableMap<Byte, StringBuffer> {
        when (tree) {
            is HuffmanLeaf -> {
                mapping[tree.value] = StringBuffer(prefix.toString())
            }
            is HuffmanNode -> {
                prefix.append('0')
                getMappingFromTree(tree.left, prefix)
                prefix.deleteCharAt(prefix.lastIndex)
                prefix.append('1')
                getMappingFromTree(tree.right, prefix)
                prefix.deleteCharAt(prefix.lastIndex)
            }
        }
        return mapping
    }

    private fun printCodes(tree: HuffmanTree, prefix: StringBuffer) {
        when (tree) {
            is HuffmanLeaf -> {
                log.debug("${tree.value.toChar()} / ${tree.value} / ${tree.value.toUByte().toInt()}\t${tree.freq}\t$prefix")
                mapping[tree.value] = StringBuffer(prefix.toString())
            }
            is HuffmanNode -> {
                //traverse left
                prefix.append('0')
                printCodes(tree.left, prefix)
                prefix.deleteCharAt(prefix.lastIndex)
                //traverse right
                prefix.append('1')
                printCodes(tree.right, prefix)
                prefix.deleteCharAt(prefix.lastIndex)
            }
        }
    }

}