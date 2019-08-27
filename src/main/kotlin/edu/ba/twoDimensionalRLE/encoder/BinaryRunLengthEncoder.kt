package edu.ba.twoDimensionalRLE.encoder

import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.extensions.reduceToSingleChar
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

    private val byteArraySize = 256
    private val analyzer = Analyzer()

    override fun encode(file: String) {
        val inputFile = File(file)
        val stream = inputFile.inputStream()
        val bytes = ByteArray(byteArraySize)
        var counter = 0
        val newFilename = "data/encoded/${getFilename(file)}"
        println("Encoding $file with chunks of size $byteArraySize bytes, encoded file will be under $newFilename")
        val fileEncoded = File(newFilename)
        val fileBinStr = File(newFilename + "_bin_str")
        val fileBinRLEStr = File(newFilename + "_bin_rle_str")
        val fileBinRLEbitEncoded = File(newFilename + "_bin_rle_nr")

        if (fileEncoded.exists()) {
            fileEncoded.delete()
            fileBinStr.delete()
            fileBinRLEStr.delete()
            fileBinRLEbitEncoded.delete()
        }

        stream.readAllBytes().forEach {
            bytes[counter++ % bytes.size] = it
            if (counter % bytes.size == 0) {
                encodeBytesToFileAsString(fileBinRLEStr, bytes)
                encodeRawBytesToFile(fileBinStr, bytes)
            }
        }
        stream.close()
        println("Finished encoding as raw bit string and as rle bit string.")
        analyzer.printFileComparison(inputFile, fileBinRLEStr)
        analyzer.printOccurrenceMap()

        println("Starting to encode the rle encoded bit string as 4 bit each (base 15)")
        fileBinRLEStr.inputStream().bufferedReader().lines().forEach { line ->
            encodeRLEtoNumberValue(line, fileBinRLEbitEncoded)
        }
        println("Finished encoding as rle encoded numerical value.")
        analyzer.printFileComparison(inputFile, fileBinRLEbitEncoded)


    }

    private fun encodeRLEtoNumberValue(line: String?, file: File) {
        FileOutputStream(file, true).bufferedWriter().use { writer ->
            var outChar: Char
            line!!.trim().split(" ").chunked(2).forEach { lineChunk ->
                outChar = lineChunk.reduceToSingleChar()
                writer.append(outChar)
            }
            writer.append("\n")
        }
    }

    private fun encodeBytesToFileAsString(file: File, bytes: ByteArray) {
        FileOutputStream(file, true).bufferedWriter().use { writer ->
            writer.write(encodeBitsAsString(bytes.toBitSetList()))
        }
    }

    private fun encodeRawBytesToFile(file: File, bytes: ByteArray) {
        FileOutputStream(file, true).bufferedWriter().use { writer ->
            writer.write(printBitString(bytes.toBitSetList()))
        }
    }


    private fun encodeBytesToFile(file: File, bytes: UByteArray) {
        file.appendBytes(encodeBitsAsString(bytes.toBitSetList()).toByteArray())
    }

    override fun decode(file: String) {
        val inputFile = File(file)
        val outputFile = File(file + "_decoded")
        var sb: StringBuilder

        if (outputFile.exists()) outputFile.delete()

        FileOutputStream(outputFile, true).bufferedWriter().use { writer ->
            inputFile.inputStream().bufferedReader().forEachLine { line ->
                sb = StringBuilder()
                line.chars().forEach {
                    sb.append(it.shr(4))
                    sb.append(" ")
                    sb.append(it.and(15))
                    sb.append(" ")

                }
                writer.write(sb.toString())
                writer.newLine()
            }

        }
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
        for (i in bitLength downTo 0) {
            var counter = 0
            var lastBit = false
            for (bitSet in listOfBits) {
                if (bitSet.get(i) == lastBit) {
                    counter++
                    if (counter == 16) {
                        stringBuilder.append("15 0 ")
                        counter = 1
                    }
                } else {
                    analyzer.occurrenceMap[counter] = analyzer.occurrenceMap.getOrDefault(counter, 0) + 1
                    stringBuilder.append("$counter ")
                    lastBit = !lastBit
                    counter = 1
                }
            }
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }

    private fun printBitString(listOfBits: List<BitSet>): String {
        val bitLength = 7
        val stringBuilder = StringBuilder()
        for (i in bitLength downTo 0) {
            for (bitSet in listOfBits) {
                if (bitSet.get(i)) {
                    stringBuilder.append("1")
                } else {
                    stringBuilder.append("0")
                }
            }
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }

    private fun encodeBitsAsByteArray(listOfBits: List<BitSet>): UByteArray {
        val bitLength = 7
        var byteList = mutableListOf<UByte>()
        for (i in bitLength downTo 0) {
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