package edu.ba.twoDimensionalRLE.encoder

import edu.ba.twoDimensionalRLE.model.Matrix
import edu.ba.twoDimensionalRLE.model.toMatrix
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.math.pow

fun runLengthEncodingBinary(text: String): ByteArray {
    return text.toByteArray()
}

fun main() {
    println(getMatrixFromString("test").toString())

}

private fun getMatrixFromString(text: String): Matrix<String> {
    val textBytes = text.toByteArray()
    val textBits = textBytes.map { it.toString(2) }.toMutableList()

    val inputSize = textBits.size.times(textBits.first().length)
    val base = ceil(sqrt(inputSize.toDouble())).toInt()

    for (i in inputSize..base.toDouble().pow(2).toInt()) {
        textBits.add("0")
    }

    var bitString = ""
    textBits.forEach { bitString += it }

    return bitString.chunked(1).asIterable().toMatrix(base, base)
}
