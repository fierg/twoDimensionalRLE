package edu.ba.twoDimensionalRLE.encoder.huffman2

import java.util.*
import kotlin.Comparator


class HuffmanEncoder(private val dictEncode: Map<Byte, StringBuffer>) {

    fun encodeIntoByteArray(input: ByteArray): ByteArray {
        val encodedText = StringBuffer()
        input.forEach { encodedText.append(dictEncode[it]) }
        val endingOffset = BYTE_SIZE - (encodedText.length % BYTE_SIZE)
        val textToEncode = encodedText.append(List(endingOffset) { _ -> '0' }.toCharArray())
        val size = 1 + encodedText.length / BYTE_SIZE + (if (endingOffset > 0) 1 else 0)
        val byteArray = ByteArray(size)

        byteArray[0] = endingOffset.toByte()
        var indexInByeArray = 1
        var indexInText = 0
        while (indexInText < textToEncode.length) {
            val byte = textToEncode.substring(indexInText, indexInText + BYTE_SIZE).toByte(2)
            byteArray[indexInByeArray] = byte
            indexInText += BYTE_SIZE
            indexInByeArray++
        }
        return byteArray
    }

    fun decode(encodedByteArray: ByteArray): ByteArray {
        val dictDecode = dictEncode.entries.asSequence().associateBy({ it.value }) { it.key }
        val offset = encodedByteArray.first().toInt()
        val byteString =
            encodedByteArray.asSequence().drop(1).map { byteToString(it) }.joinToString("").dropLast(offset)
        return decodeWithLoop(dictDecode, StringBuffer(byteString))
    }

    private fun byteToString(byte: Byte): String {
        return ("0000000" + byte.toString(2)).substring(byte.toString(2).length)
    }

    private fun decodeWithLoop(dictDecode: Map<StringBuffer, Byte>, byteString: StringBuffer): ByteArray {
        var decryptedIndex = 0
        var possibleIndex = 0
        val decryptedText = StringBuilder("")
        while (decryptedIndex < byteString.length) {
            possibleIndex += 1
            val possibleByte = StringBuffer(byteString.substring(decryptedIndex, possibleIndex))
            val word = dictDecode[possibleByte]
            if (word != null) {
                decryptedText.append(word)
                decryptedIndex = possibleIndex
            }
        }
        return decryptedText.toString().toByteArray()
    }

    companion object {
        const val BYTE_SIZE = 8

        fun of(text: ByteArray): HuffmanEncoder {
            val huffmanTree = buildHuffmanTree(text)
            val dictEncode: Map<Byte, StringBuffer> = huffmanTree.buildDictEncode()
            return HuffmanEncoder(dictEncode)
        }

        private fun buildHuffmanTree(text: ByteArray): HuffmanTree {
            val wordFrequencies = frequencies(text)
            return if (wordFrequencies.isEmpty()) {
                EmptyHuffmanTree()
            } else {
                buildHuffmanTree(wordFrequencies)
            }
        }

        private fun frequencies(text: ByteArray): List<LeafHuffmanTree> {
            return text
                .asSequence()
                .groupingBy { it }
                .eachCount()
                .toList()
                .map { LeafHuffmanTree(it.first, it.second) }
        }

        private fun buildHuffmanTree(wordFrequencies: List<HuffmanTree>): HuffmanTree {
            val queue = PriorityQueue<HuffmanTree>(
                wordFrequencies.size,
                Comparator { o1, o2 -> o1.frequency().compareTo(o2.frequency()) })
            queue.addAll(wordFrequencies)
            return buildHuffmanTree(queue)
        }

        private tailrec fun buildHuffmanTree(queue: PriorityQueue<HuffmanTree>): HuffmanTree {
            if (queue.size < 2) {
                return queue.poll()
            }
            val rightNode = queue.poll()
            val leftNode = queue.poll()
            val parentNode = NodeHuffmanTree(rightNode.frequency() + leftNode.frequency(), leftNode, rightNode)
            queue.add(parentNode)
            return buildHuffmanTree(queue)
        }
    }

}