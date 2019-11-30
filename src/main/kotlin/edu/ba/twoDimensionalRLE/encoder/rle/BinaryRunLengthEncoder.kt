package edu.ba.twoDimensionalRLE.encoder.rle

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.Encoder
import edu.ba.twoDimensionalRLE.encoder.RangedEncoder
import edu.ba.twoDimensionalRLE.extensions.pow
import edu.ba.twoDimensionalRLE.extensions.reduceToSingleChar
import edu.ba.twoDimensionalRLE.extensions.toBitSet
import edu.ba.twoDimensionalRLE.extensions.toBitSetList
import edu.ba.twoDimensionalRLE.model.DataChunk
import edu.ba.twoDimensionalRLE.model.Matrix
import edu.ba.twoDimensionalRLE.model.toMatrix
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.Map.Entry
import kotlin.experimental.or
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

class BinaryRunLengthEncoder : Encoder, RangedEncoder {

    private val log = Log.kotlinInstance()
    private val byteArraySize = 256
    private val analyzer = Analyzer()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    override fun encodeChunk(chunk: DataChunk, range: IntRange, bitsPerNumber: Int, byteSize: Int): DataChunk {
        for (index in range) {
            val currentLine = chunk.getLineFromChunk(index, byteSize)
            chunk.encodedLines[index] = encodeLineOfChunk(currentLine, byteSize, bitsPerNumber)
        }
        return chunk
    }

    private fun encodeLineOfChunk(line: ByteArray, bitSize: Int, bitsPerNumber: Int): ByteArray {
        val maxCounter = 2.pow(bitsPerNumber) - 1
        val encodedLine = encodeLineToBinRle(line, bitSize, maxCounter)
        val buffer = encodeToBinaryStringBuffer(encodedLine, bitsPerNumber)
        return buffer.toBitSet().toByteArray()
    }

    private fun encodeToBinaryStringBuffer(encodedLine: List<Int>, bitPerNumber: Int): StringBuffer {
        val result = StringBuffer()
        encodedLine.forEach {
            val bits = Integer.toBinaryString(it)
            result.append(bits.padStart(bitPerNumber, '0'))
        }
        return result
    }

    private fun encodeLineToBinRle(line: ByteArray, bitSize: Int, maxCounter: Int): List<Int> {
        val encodedLine = mutableListOf<Int>()
        var lastBit = false
        var counter = 0

        line.toBitSetList().forEach {
            for (i in bitSize downTo 0) {
                if (it[i] == lastBit) {
                    counter++
                    if (counter == maxCounter) {
                        encodedLine.add(maxCounter)
                        encodedLine.add(0)
                        counter = 1
                    }
                } else {
                    analyzer.incrementEncodingOccMap(counter)
                    encodedLine.add(counter)
                    lastBit = !lastBit
                    counter = 1
                }
            }
        }

        return encodedLine.toList()
    }

    override fun decodeChunk(chunk: DataChunk, range: IntRange, bitsPerNumber: Int, byteSize: Int): DataChunk {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun encode(inputFile: String, outputFile: String) {
        val input = File(inputFile)
        val stream = input.inputStream()
        val bytes = ByteArray(byteArraySize)
        var counter = 0
        log.info("Encoding $input with chunks of size $byteArraySize bytes, encoded file will be under $outputFile ...")
        val fileBinStr = File(outputFile + "_bin_str")
        val fileBinRLEStr = File(outputFile + "_str")
        val fileBinMapped = File(outputFile + "_str_mapped")
        val fileBinRLEbitEncoded = File(outputFile + "_nr")

//        analyzer.analyzeFile(inputFile)

        stream.readBytes().forEach { byte ->
            bytes[counter++ % bytes.size] = byte
            if (counter % bytes.size == 0) {
                encodeBytesAsStringToFile(fileBinRLEStr, bytes)
                encodeRawBytesToFile(fileBinStr, bytes)
//                encodeMappedBytesToFile(fileBinMapped, bytes)
            }
        }
        if (counter % bytes.size != 0) {
            val indexLeft = (counter % bytes.size)
            var index = 0
            val bytesLeft = ByteArray(indexLeft)

            bytes.slice(IntRange(0, (counter % bytes.size) - 1)).forEach {
                bytesLeft[index++] = it
            }
            encodeBytesAsStringToFile(fileBinRLEStr, bytesLeft)
            encodeRawBytesToFile(fileBinStr, bytesLeft)
//            encodeMappedBytesToFile(fileBinMapped, bytesLeft)

        }


        stream.close()
        log.info("Finished encoding as raw bit string and as rle bit string.")
        analyzer.printFileComparison(input, fileBinRLEStr)

        log.info("### Starting to encodeChunk the rle encoded bit string as 4 bit each (base 16)... ###")
        fileBinRLEStr.inputStream().bufferedReader().lines().forEach { line ->
            encodeRLEtoNumberValue(line, fileBinRLEbitEncoded)
        }
        log.info("Finished encoding as rle encoded numerical value.")
        analyzer.printFileComparison(input, fileBinRLEbitEncoded)
    }

    fun encodeMapped(file: String, outputFile: String): Map<Byte, Byte> {
        val analyzer = Analyzer()
        val inputFile = File(file)
        val stream = inputFile.inputStream()
        val bytes = ByteArray(byteArraySize)
        var counter = 0
        log.info("Encoding $file with chunks of size $byteArraySize bytes, remapping the input stream according to its occurrence, encoded file will be under $outputFile ...")
        val fileBinRLEStr = File(outputFile + "_str")
        val fileBinMapped = File(outputFile + "_str_mapped")
        val fileBinRLEbitEncoded = File(outputFile + "_nr")

        analyzer.analyzeFile(inputFile)
        val mapping = analyzer.getByteMapping()

        stream.readBytes().forEach { byte ->
            bytes[counter++ % bytes.size] = mapping[byte]!!
            if (counter % bytes.size == 0) {
                encodeBytesAsStringToFile(fileBinRLEStr, bytes)
                encodeRawBytesToFile(fileBinMapped, bytes)
            }
        }
        if (counter % bytes.size != 0) {
            val indexLeft = (counter % bytes.size)
            var index = 0
            val bytesLeft = ByteArray(indexLeft)

            bytes.slice(IntRange(0, (counter % bytes.size) - 1)).forEach {
                bytesLeft[index++] = it
            }
            encodeBytesAsStringToFile(fileBinRLEStr, bytesLeft)
            encodeRawBytesToFile(fileBinMapped, bytesLeft)
        }


        stream.close()
        log.info("Finished encoding as raw bit string and as rle bit string.")
        analyzer.printFileComparison(inputFile, fileBinRLEStr)

        log.info("### Starting to encodeChunk the rle encoded bit string as 4 bit each (base 16)... ###")
        fileBinRLEStr.inputStream().bufferedReader().lines().forEach { line ->
            encodeRLEtoNumberValue(line, fileBinRLEbitEncoded)
        }
        log.info("Finished encoding as rle encoded numerical value.")
        analyzer.printFileComparison(inputFile, fileBinRLEbitEncoded)

        return mapping
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

    private fun encodeBytesAsStringToFile(file: File, bytes: ByteArray) {
        FileOutputStream(file, true).bufferedWriter().use { writer ->
            writer.write(encodeBitsAsString(bytes.toBitSetList()))
        }
    }

    private fun encodeRawBytesToFile(file: File, bytes: ByteArray) {
        FileOutputStream(file, true).bufferedWriter().use { writer ->
            writer.write(printBitString(bytes.toBitSetList()))
        }
    }

    private fun encodeMappedBytesToFile(file: File, bytes: ByteArray) {
        FileOutputStream(file, true).bufferedWriter().use { writer ->
            val mapping = analyzer.getByteMapping()
            val mappedBytes = ByteArray(bytes.size)
            var index = 0
            bytes.forEach { byte ->
                mappedBytes[index++] = mapping[byte]
                    ?: throw NullPointerException("byte ${byte.toInt().toString()} not found in byte mapping!")
            }
            writer.write(printBitString(mappedBytes.toBitSetList()))
        }
    }

    @ExperimentalUnsignedTypes
    fun decodeMapped(inputFile: String, outputFile: String, mapping: Map<Byte, Byte>?) {
        val input = File(inputFile)
        val tempRLEFile = File("${outputFile}_rle_tmp")
        val tempBinFile = File("${outputFile}_bin_tmp")
        val output = File(outputFile)

        FileOutputStream(tempRLEFile).bufferedWriter().use { writer ->
            var byteQueue = LinkedList<Byte>()
            var outputSb: StringBuilder

            input.inputStream().buffered().readBytes().forEach { byte ->
                if (byte == 0xff.toByte()) {
                    outputSb = StringBuilder()
                    byteQueue.stream().forEach {
                        val uByteInt = it.toUByte().toInt()
                        if (uByteInt == 0) {
                            outputSb.append("0")
                        } else {
                            if (uByteInt.shl(4) == 0) {
                                outputSb.append(uByteInt.shr(4))
                            } else {
                                outputSb.append(uByteInt.shr(4))
                                outputSb.append(" ")
                                outputSb.append(uByteInt.and(15))
                                outputSb.append(" ")
                            }

                        }
                    }
                    writer.write(outputSb.toString())
                    writer.newLine()
                    byteQueue = LinkedList()

                } else {
                    byteQueue.add(byte)
                }
            }
        }

        log.info("parsed internal encoding to numerical run length encoding on mapped binary data.")
        log.info("decoding back to mapped byte String...")

        val chunks = mutableListOf<ByteArray>()
        var currentChunkBytes = ByteArray(byteArraySize)
        var lineCounter = 0
        var linePositionCounter = 0
        var lastSeenLinePosCounter = 0
        FileOutputStream(tempBinFile).bufferedWriter().use { writer ->
            tempRLEFile.inputStream().bufferedReader().lines().forEach { line ->

                if (lineCounter != 0 && lineCounter % 8 == 0) {
                    chunks.add(currentChunkBytes)
                    currentChunkBytes = ByteArray(byteArraySize)
                }

                var lastBit = 0
                val sb = StringBuilder()
                val counterList = mutableListOf<Int>()
                var readZero = false
                var lineTokens = line.trim().split(" ")

                if (lineTokens.first().toInt() == 0) {
                    lineTokens = lineTokens.drop(1)
                    lastBit = 1
                }

                lineTokens.forEach {
                    if (!readZero) {
                        counterList.add(it.toInt())
                    } else {
                        if (it.toInt() != 0) {
                            counterList[counterList.size] = counterList.last() + it.toInt()
                            readZero = false
                        } else {
                            readZero = true
                        }
                    }
                }
                for (i in counterList) {
                    for (j in 0 until i) {
                        if (lastBit == 1) {
                            currentChunkBytes[linePositionCounter] =
                                currentChunkBytes[linePositionCounter].or((2.pow(7 - (lineCounter % 8))).toByte())
                        }
                        linePositionCounter++
                        sb.append(lastBit)
                    }
                    lastBit = if (lastBit == 0) 1 else 0
                }
                lineCounter++
                lastSeenLinePosCounter = linePositionCounter
                linePositionCounter = 0
                sb.append("\n")
                writer.write(sb.toString())
            }
            currentChunkBytes = currentChunkBytes.dropLast(byteArraySize - lastSeenLinePosCounter).toByteArray()
            chunks.add(currentChunkBytes)
        }

        log.info("parsed numerical run length encoding on mapped binary data.")
        log.info("reverting binary shift and started to encodeChunk to original byte stream...")

        FileOutputStream(output).buffered().use { writer ->
            chunks.forEach { byteArray ->
                writer.write(remapByteArray(byteArray, mapping))
            }
        }

        log.info("finished remapping & decoding original byte stream.")
    }

    @ExperimentalUnsignedTypes
    override fun decode(inputFile: String, outputFile: String) {
        val input = File(inputFile)
        val tempRLEFile = File("${outputFile}_rle_tmp")
        val tempBinFile = File("${outputFile}_bin_tmp")
        val output = File(outputFile)

        FileOutputStream(tempRLEFile).bufferedWriter().use { writer ->
            var byteQueue = LinkedList<Byte>()
            var outputSb: StringBuilder

            input.inputStream().buffered().readBytes().forEach { byte ->
                if (byte == 0xff.toByte()) {
                    outputSb = StringBuilder()
                    byteQueue.stream().forEach {
                        val uByteInt = it.toUByte().toInt()
                        if (uByteInt == 0) {
                            outputSb.append("0")
                        } else {
                            if (uByteInt.shl(4) == 0) {
                                outputSb.append(uByteInt.shr(4))
                            } else {
                                outputSb.append(uByteInt.shr(4))
                                outputSb.append(" ")
                                outputSb.append(uByteInt.and(15))
                                outputSb.append(" ")
                            }

                        }
                    }
                    writer.write(outputSb.toString())
                    writer.newLine()
                    byteQueue = LinkedList()

                } else {
                    byteQueue.add(byte)
                }
            }

        }

        log.info("parsed internal encoding to numerical run length encoding on binary data.")
        log.info("decoding back to original byte String...")

        val chunks = mutableListOf<ByteArray>()
        var currentChunkBytes = ByteArray(byteArraySize)
        var lineCounter = 0
        var linePositionCounter = 0
        var lastSeenLinePosCounter = 0
        FileOutputStream(tempBinFile).bufferedWriter().use { writer ->
            tempRLEFile.inputStream().bufferedReader().lines().forEach { line ->

                if (lineCounter != 0 && lineCounter % 8 == 0) {
                    chunks.add(currentChunkBytes)
                    currentChunkBytes = ByteArray(byteArraySize)
                }

                var lastBit = 0
                val sb = StringBuilder()
                val counterList = mutableListOf<Int>()
                var readZero = false
                var lineTokens = line.trim().split(" ")

                if (lineTokens.first().toInt() == 0) {
                    lineTokens = lineTokens.drop(1)
                    lastBit = 1
                }

                lineTokens.forEach {
                    if (!readZero) {
                        counterList.add(it.toInt())
                    } else {
                        if (it.toInt() != 0) {
                            counterList[counterList.size] = counterList.last() + it.toInt()
                            readZero = false
                        } else {
                            readZero = true
                        }
                    }
                }
                for (i in counterList) {
                    for (j in 0 until i) {
                        if (lastBit == 1) {
                            currentChunkBytes[linePositionCounter] =
                                currentChunkBytes[linePositionCounter].or((2.pow(7 - (lineCounter % 8))).toByte())
                        }
                        linePositionCounter++
                        sb.append(lastBit)
                    }
                    lastBit = if (lastBit == 0) 1 else 0
                }
                lineCounter++
                lastSeenLinePosCounter = linePositionCounter
                linePositionCounter = 0
                sb.append("\n")
                writer.write(sb.toString())
            }
            currentChunkBytes = currentChunkBytes.dropLast(byteArraySize - lastSeenLinePosCounter).toByteArray()
            chunks.add(currentChunkBytes)
        }

        log.info("parsed numerical run length encoding on binary data.")
        log.info("reverting binary shift and started to encodeChunk to original byte stream...")

        FileOutputStream(output).buffered().use { writer ->
            chunks.forEach { byteArray ->
                writer.write(byteArray)
            }
        }

        log.info("finished decoding original byte stream.")
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
                    analyzer.incrementEncodingOccMap(counter)
                    stringBuilder.append("$counter ")
                    lastBit = !lastBit
                    counter = 1
                }
            }
            stringBuilder.append(counter)
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

    @ExperimentalUnsignedTypes
    private fun encodeBitsAsByteArray(listOfBits: List<BitSet>): UByteArray {
        val bitLength = 7
        val byteList = mutableListOf<UByte>()
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
        val regex = Regex(".+/(.+)")
        if (file.matches(regex)) {
            return regex.find(file)!!.groupValues[1]
        } else {
            throw IllegalArgumentException("Given Filename doesn't match default file location and can't be parsed.")
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

    private fun remapByteArray(bytes: ByteArray, mapping: Map<Byte, Byte>?): ByteArray {
        val result = ByteArray(bytes.size)
        var index = 0
        val inverseMapping =
            mapping!!.entries.stream().collect(Collectors.toMap(Entry<Byte, Byte>::value, Entry<Byte, Byte>::key))

        bytes.forEach { byte ->
            result[index++] = inverseMapping[byte]!!
        }
        return result
    }
}

