package edu.ba.twoDimensionalRLE.encoder.huffman2

import java.util.*

abstract class HuffmanTree : HasFrequency {
    fun buildDictEncode(): Map<Byte, StringBuffer> {
        return buildPrefixTree(LinkedList(listOf(Pair(StringBuffer(), this))), mutableMapOf())
    }

    private tailrec fun buildPrefixTree(queue: LinkedList<Pair<StringBuffer, HuffmanTree>>, acc: MutableMap<Byte, StringBuffer>): Map<Byte, StringBuffer> {
        if (queue.isEmpty()) return acc

        val smallestHuffmanTree = queue.poll()
        val tree = smallestHuffmanTree.second
        val prefix = smallestHuffmanTree.first
        when (tree) {
            is NodeHuffmanTree -> {
                queue.add(Pair(prefix.append('0'), tree.leftChild))
                queue.add(Pair(prefix.append('1'), tree.rightChild))
            }
            is LeafHuffmanTree -> {
                acc[tree.word] = prefix
            }
        }
        return buildPrefixTree(queue, acc)
    }
}