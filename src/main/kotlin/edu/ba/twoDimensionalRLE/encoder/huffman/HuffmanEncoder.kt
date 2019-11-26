package edu.ba.twoDimensionalRLE.encoder.huffman


import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.encoder.Encoder
import edu.ba.twoDimensionalRLE.encoder.RangedEncoder
import edu.ba.twoDimensionalRLE.extensions.*
import edu.ba.twoDimensionalRLE.model.DataChunk
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or

class HuffmanEncoder : Encoder, RangedEncoder {
    private val log = Log.kotlinInstance()
    private val mapping = mutableMapOf<Byte, StringBuffer>()
    private val byteArraySize = 256
    private val bitSize = 8


    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    override fun decodeChunk(chunk: DataChunk, range: IntRange): DataChunk {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun encode(inputFile: String, outputFile: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun encodeChunk(chunk: DataChunk, range: IntRange): DataChunk {
        for (index in range) {
        }
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    }

    override fun decode(inputFile: String, outputFile: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun encode(bytes: ByteArray, lines: Int) {
        log.info("Encoding last $lines lines with chunks of size $byteArraySize bytes")
        encodeLine(bytes, lines)
    }


    fun encodeLine(bytes: ByteArray, line: Int): ByteArray {
        assert(bitSize >= line)
        val noOfChars = bytes.size / bitSize
        val currentLineAsByteArray = ByteArray(noOfChars)
        var byteCounter = 0
        bytes.forEachIndexed { index, byte ->
            if (index != 0 && index % bitSize == 0)
                byteCounter++
            if (byte.and(2.pow(line).toByte()) == 2.pow(line).toByte())
                currentLineAsByteArray[byteCounter] =
                    currentLineAsByteArray[byteCounter].or(2.pow(index % bitSize).toByte())
        }

        val huffmanMapping = getHuffmanMapping(noOfChars, currentLineAsByteArray)

        return writeEncodedAsStringBuffer(currentLineAsByteArray, huffmanMapping)
    }

    fun getHuffmanMapping(noOfChars: Int, currentLineAsByteArray: ByteArray): MutableMap<Byte, StringBuffer> {
        val huffmanTree = buildTree(calcFrequencies(currentLineAsByteArray, noOfChars))
        printCodes(huffmanTree, StringBuffer())
        return mapping
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
            val orderPosition = bitSize - 1 - (currentBitPosition % bitSize)
            val gapSize = orderPosition - (currentBitPosition % bitSize)


            if (gapSize >= mappedByte.second) {
                // huffman code fits into current byte

                val shiftSize = orderPosition + 1 - mappedByte.second
                currentByteVal = currentByteVal.or(mappedByte.first.toShiftedLeftByte(shiftSize))
                result[currentBytePosition] = currentByteVal

                currentBitPosition += mappedByte.second
                currentBytePosition = currentBitPosition / bitSize
            } else {
                //it doesn't
                val secondHalfSize = mappedByte.second - gapSize

                currentByteVal = currentByteVal.or(mappedByte.first.toShiftedRightByte(secondHalfSize))
                result[currentBytePosition] = currentByteVal

                currentBitPosition += gapSize
                currentBytePosition = currentBitPosition / bitSize
                val shiftSize = bitSize - secondHalfSize

                currentByteVal = result.getOrElse(currentBytePosition) { 0.toByte() }
                currentByteVal = currentByteVal.or(mappedByte.first.toShiftedLeftByte(shiftSize))
                result[currentBytePosition] = currentByteVal

                currentBitPosition += secondHalfSize
                currentBytePosition = currentBitPosition / bitSize
            }
        }
        result
    }

    private fun buildTree(charFreqs: IntArray): HuffmanTree {
        val trees = PriorityQueue<HuffmanTree>()

        charFreqs.forEachIndexed { index, freq ->
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

    fun calcFrequencies(bytes: ByteArray, maxSymbols: Int): IntArray {
        // calc frequencies
        val freqs = IntArray(maxSymbols)
        bytes.forEach { symbol -> freqs[symbol.index()] = freqs[symbol.index()] + 1 }
        return freqs
    }


    fun printCodes(tree: HuffmanTree, prefix: StringBuffer) {
        when (tree) {
            is HuffmanLeaf -> {
                log.debug("${tree.value.toChar()} / ${tree.value}\t${tree.freq}\t$prefix")
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
