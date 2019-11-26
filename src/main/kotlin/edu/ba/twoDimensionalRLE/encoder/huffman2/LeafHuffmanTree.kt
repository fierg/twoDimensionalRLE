package edu.ba.twoDimensionalRLE.encoder.huffman2

data class LeafHuffmanTree(val word: Byte, val frequency: Int) : HuffmanTree() {
    override fun frequency(): Int {
        return frequency
    }
}