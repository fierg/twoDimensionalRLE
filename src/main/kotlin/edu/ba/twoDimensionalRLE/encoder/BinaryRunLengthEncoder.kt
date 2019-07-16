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

class BinaryRunLengthEncoder : Encoder {

    private val byteArraySize = 1024
    private val occurrenceMap = mutableMapOf<Int, Int>()

    override fun encode(file: String) {
        val inputFile = File(file)
        val stream = inputFile.inputStream()
        val bytes = ByteArray(byteArraySize)
        var counter = 0
        val newFilename = "data/encoded/${getFilename(file)}"
        println("Encoding $file with chunks of size $byteArraySize bytes, encoded file will be under $newFilename")
        val fileStr = File(newFilename)
        val fileBin = File(newFilename + "_bin")

        if (fileStr.exists()) {
            fileStr.delete()
            fileBin.delete()
        }
        fileStr.createNewFile()

        stream.readAllBytes().forEach {
            bytes[counter++ % bytes.size] = it
            if (counter % bytes.size == 0) {
                encodeBytesToFileAsString(fileStr, bytes)
                encodeBytesToFile(fileBin, bytes.toUByteArray())
            }
        }
        stream.close()

        println("Finished encoding. Encoded File has a file size of ${fileStr.length() / 1000000.toDouble()} MB")
        println("${(fileStr.length() / inputFile.length().toDouble())} times the original size")
        println("occurrence of repetition counts: ")
        occurrenceMap.keys.sorted().forEach { println("$it ${occurrenceMap[it]}") }
    }

    private fun encodeBytesToFileAsString(file: File, bytes: ByteArray) {
        FileOutputStream(file, true).bufferedWriter().use { writer ->
            writer.write(encodeBitsAsString(bytes.toBitSetList()))
        }
    }

    private fun encodeBytesToFile(file: File, bytes: UByteArray) {
        file.appendBytes(encodeBitsAsString(bytes.toBitSetList()).toByteArray())
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

    private fun encodeBitsAsString(listOfBits: List<BitSet>): String {
        val bitLength = 7
        val stringBuilder = StringBuilder()
        for (i in 0..bitLength) {
            var counter = 0
            var lastBit = false
            for (bitSet in listOfBits) {
                if (bitSet.get(i) == lastBit) {
                    counter++
                } else {
                    occurrenceMap[counter] = occurrenceMap.getOrDefault(counter, 0) + 1
                    stringBuilder.append(counter)
                    lastBit = !lastBit
                    counter = 1
                }
            }
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }

    private fun encodeBitsAsByteArray(listOfBits: List<BitSet>): UByteArray {
        val bitLength = 7
        var byteList = mutableListOf<UByte>()
        for (i in 0..bitLength) {
            var counter = 0
            var lastBit = false
            for (bitSet in listOfBits) {
                if (bitSet.get(i) == lastBit) {
                    counter++
                } else {
                    byteList.add(counter.toUByte())
                    lastBit = !lastBit
                    counter = 1
                }
            }
            byteList.add(byteArraySize.toUByte())
        }

        return byteList.toUByteArray()
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