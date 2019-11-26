package edu.ba.twoDimensionalRLE.encoder.huffman2

class EmptyHuffmanTree : HuffmanTree() {
    override fun frequency(): Int {
        return 0
    }
}