package edu.ba.twoDimensionalRLE.encoder

interface AbstractEncoder {
    fun encode(file: String)
    fun decode(file: String)
}