package edu.ba.twoDimensionalRLE.encoder.huffman


import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.encoder.Encoder
import edu.ba.twoDimensionalRLE.encoder.RangedEncoder
import edu.ba.twoDimensionalRLE.extensions.pow
import edu.ba.twoDimensionalRLE.extensions.toBitSet
import edu.ba.twoDimensionalRLE.model.DataChunk
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or

class HuffmanEncoder : Encoder, RangedEncoder {
    private val log = Log.kotlinInstance()
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


    fun encodeLine(bytes: ByteArray, line: Int) {
        assert(bitSize > line)
        val noOfChars = bytes.size / bitSize
        val chars = ByteArray(noOfChars)
        var byteCounter = 0
        bytes.forEachIndexed { index, byte ->
            if (index != 0 && index % bitSize == 0)
                byteCounter++
            if (byte.and(2.pow(line).toByte()) == 2.pow(line).toByte())
                chars[byteCounter] = chars[byteCounter].or(2.pow(index % bitSize).toByte())
        }

        val ints = IntArray(noOfChars)
        chars.forEachIndexed { index, byte -> ints[index] = byte.toInt() }

        val huffmanMapping = getMappingFromStrBuffer(buildTree(ints))
        //TODO encode mapped chars
    }

    private fun buildTree(charFreqs: IntArray): HuffmanTree {
        val trees = PriorityQueue<HuffmanTree>()

        charFreqs.forEachIndexed { index, freq ->
            if (freq > 0) trees.offer(HuffmanLeaf(freq, index.toChar()))
        }

        assert(trees.size > 0)
        while (trees.size > 1) {
            val a = trees.poll()
            val b = trees.poll()
            trees.offer(HuffmanNode(a, b))
        }

        return trees.poll()
    }
    fun printCodes(tree: HuffmanTree, prefix: StringBuffer) {
        when (tree) {
            is HuffmanLeaf -> log.debug("${tree.value}\t${tree.freq}\t$prefix")
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

    private fun getMappingFromStrBuffer(tree: HuffmanTree, prefix: StringBuffer, mapping: MutableMap<Byte, String>): MutableMap<Byte, String> {
        when (tree) {
            is HuffmanLeaf -> mapping[tree.value.toByte()] = prefix.toString()
            is HuffmanNode -> {
                //traverse left
                prefix.append('0')
                getMappingFromStrBuffer(tree.left, prefix, mapping)
                prefix.deleteCharAt(prefix.lastIndex)
                //traverse right
                prefix.append('1')
                getMappingFromStrBuffer(tree.right, prefix, mapping)
                prefix.deleteCharAt(prefix.lastIndex)
            }
        }
        return mapping
    }

    fun getMappingFromStrBuffer(tree: HuffmanTree): MutableMap<Byte, String> {
        return getMappingFromStrBuffer(tree, StringBuffer(), mutableMapOf<Byte, String>())
    }

    private fun getMappingFromTree(tree: HuffmanTree, index: Int, prefixBitSet: BitSet, mapping: MutableMap<Byte, BitSet>): MutableMap<Byte, BitSet> {
        val prefixBitSet = prefixBitSet
        val mapping = mapping
        var index = index


        when (tree) {
            is HuffmanLeaf -> mapping[tree.value.toByte()] = prefixBitSet
            is HuffmanNode -> {
                //traverse left
                prefixBitSet.set(index++, false)
                getMappingFromTree(tree.left, index, prefixBitSet, mapping)
                prefixBitSet.clear(prefixBitSet.length())
                index--
                //traverse right
                prefixBitSet.set(index++, true)
                getMappingFromTree(tree.right, index, prefixBitSet, mapping)
                prefixBitSet.clear(prefixBitSet.length())
            }
        }

        return mapping
    }

    private fun getMappingFromTree(tree: HuffmanTree): MutableMap<Byte, BitSet> {
        return getMappingFromTree(tree, 0 , BitSet(), mutableMapOf())
    }
}
