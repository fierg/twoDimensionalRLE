package edu.ba.twoDimensionalRLE.encoder.huffman

abstract class HuffmanTree(var frequency: Int) : Comparable<HuffmanTree> {
    override fun compareTo(other: HuffmanTree) = frequency - other.frequency
}