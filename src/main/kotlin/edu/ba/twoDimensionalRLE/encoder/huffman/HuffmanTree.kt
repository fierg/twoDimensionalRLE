package edu.ba.twoDimensionalRLE.encoder.huffman

abstract class HuffmanTree(var freq: Int) : Comparable<HuffmanTree> {
    override fun compareTo(other: HuffmanTree) = freq - other.freq
}