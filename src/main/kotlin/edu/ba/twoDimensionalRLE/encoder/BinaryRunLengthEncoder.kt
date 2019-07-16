package edu.ba.twoDimensionalRLE.encoder

import edu.ba.twoDimensionalRLE.extensions.toBitSetList
import edu.ba.twoDimensionalRLE.model.Matrix
import edu.ba.twoDimensionalRLE.model.toMatrix
import java.io.File
import java.util.*
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.math.pow

fun runLengthEncodingBinary(fileName: String) {
    val stream = File(fileName).inputStream()
    val bytes = ByteArray(16)
    var counter = 0
    stream.readAllBytes().forEach {
        bytes[counter++ % 16] = it
        if (counter % 16 == 0) {
            encodeBits(bytes.toBitSetList())
        }
    }
    stream.close()
}

fun main() {
    runLengthEncodingBinary("data/t8.shakespeare.txt")
    println(getSquareMatrixFromString("testsliqSFBJLQHWliaUSFHQLJAHSVJ SQFAF").toString())

}

private fun getSquareMatrixFromString(text: String): Matrix<String> {
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

private fun encodeBits(listOfBits: List<BitSet>): String {
    return ""
}
