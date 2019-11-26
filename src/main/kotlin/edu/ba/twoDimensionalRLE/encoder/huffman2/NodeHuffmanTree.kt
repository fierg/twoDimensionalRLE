package edu.ba.twoDimensionalRLE.encoder.huffman2

 data class NodeHuffmanTree(val frequency: Int, val leftChild: HuffmanTree, val rightChild: HuffmanTree) :
    HuffmanTree() {
    override fun frequency(): Int {
        return frequency
    }
}