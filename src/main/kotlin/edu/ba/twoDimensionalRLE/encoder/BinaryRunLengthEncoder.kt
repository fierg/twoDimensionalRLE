package edu.ba.twoDimensionalRLE.encoder

import edu.ba.twoDimensionalRLE.extensions.toBitSetList
import edu.ba.twoDimensionalRLE.model.Matrix
import edu.ba.twoDimensionalRLE.model.toMatrix
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.math.pow

class BinaryRunLengthEncoder : AbstractEncoder {

    private val byteArraySize = 2048

    override fun encode(file: String) {
        val stream = File(file).inputStream()
        val bytes = ByteArray(byteArraySize)
        var counter = 0
        val newFilename = "data/encoded/${getFilename(file)}"
        val file = File(newFilename)

        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()

        stream.readAllBytes().forEach {
            bytes[counter++ % bytes.size] = it
            if (counter % bytes.size == 0) {
                encodeBytesToFile(file, bytes)
            }
        }
        stream.close()

        println("${file.length() / 1000000.toDouble() } MB File size")
    }

    private fun encodeBytesToFile(file: File, bytes: ByteArray) {
        FileOutputStream(file, true).bufferedWriter().use { writer ->
            writer.write(encodeBits(bytes.toBitSetList()))
        }
    }

    override fun decode(file: String) {
        TODO("not implemented")
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
        val bitLength = 7
        val stringBuilder = StringBuilder()
        for (i in 0..bitLength) {
            var counter = 0
            var lastBit = false
            for (bitSet in listOfBits) {
                if (bitSet.get(i) == lastBit) {
                    counter++
                } else {
                    stringBuilder.append(counter)
                    lastBit = !lastBit
                    counter = 1
                }
            }
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }

    private fun getFilename(file: String): String {
        val regex = Regex(".+\\/(.+)")
        if (file.matches(regex)) {
            return regex.find(file)!!.groupValues[1]
        } else {
            throw IllegalArgumentException("Given Filename doesn't match default file location and cant be parsed.")
        }
    }
}

fun main() {
    val binaryRunLengthEncoder = BinaryRunLengthEncoder()
    binaryRunLengthEncoder.encode("data/t8.shakespeare.txt")

}