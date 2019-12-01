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

class HuffmanEncoder : Encoder, RangedEncoder {
    private val log = Log.kotlinInstance()
    val mapping = mutableMapOf<Byte, StringBuffer>()
    private val byteArraySize = 256
    private val byteSize = 8

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    override fun decodeChunkRLE(
        chunk: DataChunk,
        range: IntRange,
        bitsPerNumber: Int,
        byteSize: Int,
        rleNumbers: List<Int>
    ): DataChunk {
        throw NotImplementedError()
    }

    @ExperimentalUnsignedTypes
    override fun encode(inputFile: String, outputFile: String) {
        log.info("Parsing input file $inputFile ...")
        val bytes = File(inputFile).readBytes()

        log.info("Building tree to generate Huffman mapping...")
        val huffmanMapping = getHuffmanMapping(bytes.size, bytes)
        log.info("Finished building tree.")

        log.info("Writing mapped input file as binary stream to $outputFile")
        BitStream(File(outputFile).openBinaryStream(false)).use { stream ->
            bytes.forEach { byte ->
                huffmanMapping[byte]?.writeToBinaryStream(stream)
            }
        }
        log.info("Finished huffman encoding.")
    }

    @ExperimentalUnsignedTypes
    override fun encodeChunk(chunk: DataChunk, range: IntRange, bitsPerNumber: Int, byteSize: Int): DataChunk {
        val totalByteArraySize = byteArraySize * (range.last - range.first + 1)
        var linesToEncode = ByteArray(totalByteArraySize)

        for (index in range) {
            assert(index in 0 until byteSize)
            linesToEncode += chunk.getLineFromChunk(index, bitSize = 8)
        }
        val huffmanMapping = getHuffmanMapping(totalByteArraySize, linesToEncode)
        chunk.encodedLines[range.last] = writeEncodedAsStringBuffer(linesToEncode, huffmanMapping)
        return chunk
    }

    @ExperimentalUnsignedTypes
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

    @ExperimentalUnsignedTypes
    override fun decode(inputFile: String, outputFile: String) {
        BitStream(File(inputFile).openBinaryStream(false)).use { stream ->
            while (stream.bitPosition < stream.size * 8) {
                print(if (stream.readBit()) "1" else "0")
            }
        }
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ExperimentalUnsignedTypes
    fun decodeFileInternal(inputFile: String, outputFile: String, huffmanMapping: Map<StringBuffer, Byte>) {
        val smallestMapping = huffmanMapping.keys.minBy { it.length }!!.length
        val largesMapping = huffmanMapping.keys.maxBy { it.length }!!.length
        var currentMappingSize = smallestMapping
        var currentPrefixSeen: StringBuffer
        val decodingResult = mutableListOf<Byte>()
        val stringMapping =
            huffmanMapping.map { entry: Map.Entry<StringBuffer, Byte> -> entry.key.toString() to entry.value }.toMap()

        log.info("Starting to decode huffman encoding from $inputFile ...")

        BitStream(File(inputFile).openBinaryStream(false)).use { stream ->
            while ((stream.bitPosition + currentMappingSize) < stream.size * 8) {
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
        }

        log.info("Finished decoding. Writing decoded stream to $outputFile ...")
        File(outputFile).writeBytes(decodingResult.toByteArray())
        log.info("Finished decoding.")
    }

    fun decode(encodedByteArray: ByteArray, dictEncode: Map<Byte, StringBuffer>): ByteArray {
        val dictDecode = dictEncode.entries.asSequence().associateBy({ it.value }) { it.key }
        val offset = encodedByteArray.first().toInt()
        val byteString =
            encodedByteArray.asSequence().drop(1).map { byteToString(it) }.joinToString("").dropLast(offset)
        return decodeWithLoop(dictDecode, byteString)
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

    @ExperimentalUnsignedTypes
    fun getHuffmanMapping(noOfChars: Int, byteArray: ByteArray): MutableMap<Byte, StringBuffer> {
        val huffmanTree = buildTree(countOccurrences(byteArray, noOfChars))
        return getMappingFromTree(huffmanTree, StringBuffer())
    }

    private fun writeEncodedAsStringBuffer(bytes: ByteArray, mapping: Map<Byte, StringBuffer>): ByteArray {
        val result = StringBuffer()
        bytes.forEach {
            result.append(mapping[it])
        }
        return result.toBitSet().toByteArray()
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

    @ExperimentalUnsignedTypes
    private fun countOccurrences(bytes: ByteArray, maxSymbols: Int): IntArray {
        val frequencies = IntArray(256)
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

    @ExperimentalUnsignedTypes
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
