package edu.ba.twoDimensionalRLE.encoder

import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.extensions.reduceToSingleChar
import edu.ba.twoDimensionalRLE.extensions.toBitSetList
import edu.ba.twoDimensionalRLE.model.Matrix
import edu.ba.twoDimensionalRLE.model.toMatrix
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

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
        if (counter % bytes.size != 0) {
            bytes.slice(IntRange(0, counter % bytes.size))
            encodeBytesToFileAsString(fileBinRLEStr, bytes)
            encodeRawBytesToFile(fileBinStr, bytes)
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
        FileOutputStream(file, true).use { writer ->
            var outChar: Char
            line!!.trim().split(" ").chunked(2).forEach { lineChunk ->
                outChar = lineChunk.reduceToSingleChar()
                writer.write(outChar.toInt())
            }
            writer.write(0xFF)
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
        val outputFile = File("data/decoded/${inputFile.nameWithoutExtension}_decoded.txt")

        if (outputFile.exists()) outputFile.delete()

        FileOutputStream(outputFile, true).bufferedWriter().use { writer ->

            val inputStream = inputFile.inputStream()
            var byteQueue = LinkedList<Byte>()
            var outputSb: StringBuilder

            inputStream.buffered().readAllBytes().iterator().forEach { byte ->
                if (byte == 0xff.toByte()) {
                    outputSb = StringBuilder()

                    byteQueue.stream().forEach {
                        outputSb.append(it.toUByte().toInt().shr(4))
                        outputSb.append(" ")
                        outputSb.append(it.toUByte().toInt().and(15))
                        outputSb.append(" ")
                    }
                    writer.write(outputSb.toString())
                    writer.newLine()
                    byteQueue = LinkedList()

                } else {
                    byteQueue.add(byte)
                }
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

    private fun readUpToNull(inputStream: InputStream): String {
        return buildString {
            while (true) {
                val ch = inputStream.read().toChar()
                if (ch == '\u0000') break
                append(ch)
            }
        }
    }
}