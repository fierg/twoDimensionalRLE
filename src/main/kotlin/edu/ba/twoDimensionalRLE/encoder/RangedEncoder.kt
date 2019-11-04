package edu.ba.twoDimensionalRLE.encoder

interface RangedEncoder : Encoder {
    fun encode(inputFile: String, outputFile: String, range: IntRange)
    fun decode(inputFile: String, outputFile: String, range: IntRange)
}