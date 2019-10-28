package edu.ba.twoDimensionalRLE.encoder.huffman

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.extensions.pow
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or

class HuffmanEncoder {
    private val log = Log.kotlinInstance()
    private val byteArraySize = 256
    private val bitSize = 8

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    fun decode(inputFile: String, outputFile: String) {
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
        chars.forEachIndexed{index, byte -> ints[index] = byte.toInt()  }

        buildTree(ints)

    }


    fun buildTree(charFreqs: IntArray): HuffmanTree {
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
            is HuffmanLeaf -> println("${tree.value}\t${tree.freq}\t$prefix")
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


fun main(args: Array<String>) {
    val test = "this is an example for huffman encoding"

    val maxIndex = test.max()!!.toInt() + 1
    val freqs = IntArray(maxIndex) //256 enough for latin ASCII table, but dynamic size is more fun
    test.forEach { freqs[it.toInt()] += 1 }

    val encoder = HuffmanEncoder()
    val tree = encoder.buildTree(freqs)
    println("SYMBOL\tWEIGHT\tHUFFMAN CODE")
    encoder.printCodes(tree, StringBuffer())
    tree
}
