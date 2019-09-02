package edu.ba.twoDimensionalRLE.encoder

interface Encoder {
    fun encode(inputFile: String, outputFile: String)
    fun decode(inputFile: String, outputFile: String)
}