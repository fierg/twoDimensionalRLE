package edu.ba.twoDimensionalRLE.encoder.huffman

class HuffmanNode(var left: HuffmanTree, var right: HuffmanTree) : HuffmanTree(left.frequency + right.frequency)
