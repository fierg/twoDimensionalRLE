package edu.ba.twoDimensionalRLE.model

import de.jupf.staticlog.Log
import de.jupf.staticlog.core.Logger
import edu.ba.twoDimensionalRLE.extensions.isWholeNumber
import edu.ba.twoDimensionalRLE.extensions.pow
import loggersoft.kotlin.streams.BitStream
import loggersoft.kotlin.streams.openBinaryStream
import loggersoft.kotlin.streams.toIntUnsigned
import java.io.File
import java.io.FileOutputStream
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.ceil
import kotlin.math.max

open class DataChunk(val input: ByteArray) {

    private val log = Log.kotlinInstance()
    val encodedLines = mutableMapOf<Int, ByteArray>()
    val decodedLines = mutableMapOf<Int, ByteArray>()
    val bytes = input.clone()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    companion object {
        private val DEBUG = true

        internal fun readChunksFromFile(inputFile: String, byteArraySize: Int, log: Logger): MutableList<DataChunk> {
            val input = File(inputFile)
            val bytes = ByteArray(byteArraySize)
            val chunks = mutableListOf<DataChunk>()
            var counter = 0
            log.info("Reading $input into chunks of size $byteArraySize bytes...")

            input.inputStream().readBytes().forEach { byte ->
                bytes[counter++ % bytes.size] = byte
                if (counter % bytes.size == 0) {
                    chunks.add(DataChunk(bytes))
                }
            }
            if (counter % bytes.size != 0) {
                val indexLeft = (counter % bytes.size)
                var index = 0
                val bytesLeft = ByteArray(indexLeft)

                bytes.slice(IntRange(0, (counter % bytes.size) - 1)).forEach {
                    bytesLeft[index++] = it
                }
                chunks.add(DataChunk(bytesLeft))
            }
            log.info("Finished reading input into ${chunks.size} chunks.")
            return chunks
        }

        @ExperimentalUnsignedTypes
        @Deprecated("Use mixed encoders methods instead! This is debug only!")
        fun debugReadFromEncodedFile(inputFile: String, byteArraySize: Int, log: Logger): List<DataChunk> {
            val input = File(inputFile)
            val chunks = mutableListOf<DataChunk>()
            var currentByte: Byte
            val currentBytes = mutableListOf<Byte>()
            var line = 0
            var currentChunk = DataChunk(ByteArray(0))


            log.info("Reading $input into chunks of size $byteArraySize bytes...")

            if (DEBUG) {
                BitStream(File(input.toURI()).openBinaryStream(true)).use { stream ->
                    while (stream.bitPosition < stream.size * 8) {
                        print(if (stream.readBit()) "1" else "0")
                    }
                }
                println()

                BitStream(File(input.toURI()).openBinaryStream(true)).use { stream ->
                    while (stream.position < stream.size) {
                        print(stream.readByte().toChar())
                    }
                }

                println()

                BitStream(File(input.toURI()).openBinaryStream(true)).use { stream ->
                    while (stream.position < stream.size) {
                        print(stream.readByte().toIntUnsigned())
                        print(" ")
                    }
                }
            }

            BitStream(File(input.toURI()).openBinaryStream(false)).use { stream ->
                while (stream.bitPosition < stream.size * 8) {
                    currentByte = stream.readByte()

                    if (line % 8 == 0 && line != 0) {
                        chunks.add(currentChunk)
                        currentChunk = DataChunk(ByteArray(0))
                    }
                    while (currentByte != 0.toByte() && stream.bitPosition < stream.size * 8) {
                        currentBytes.add(currentByte)
                        currentByte = stream.readByte()
                    }

                    currentChunk.encodedLines[line++ % 8] = currentBytes.toByteArray()
                }
                if (line % 8 == 0 && line != 0) {
                    chunks.add(currentChunk)
                    currentChunk = DataChunk(ByteArray(0))
                } else {
                    log.warn("Unexpected number of lines parsed! Lines: $line")
                }
            }

            return chunks
        }

    }

    fun getLineFromChunk(line: Int, bitSize: Int): ByteArray {
        assert(bitSize >= line)
        var noOfChars = bytes.size / bitSize.toDouble()
        if (!noOfChars.isWholeNumber()) {
            noOfChars = ceil(noOfChars)
        }

        val chars = ByteArray(noOfChars.toInt())
        var byteCounter = 0
        bytes.forEachIndexed { index, byte ->
            if (index != 0 && index % bitSize == 0)
                byteCounter++
            if (byte.and(2.pow(line).toByte()) == 2.pow(line).toByte())
                chars[byteCounter] = chars[byteCounter].or(2.pow(index % bitSize).toByte())
        }
        return chars
    }

    fun writeDecodedLinesToChunk(){

    }

    fun applyByteMapping(mapping: Map<Byte, Byte>): DataChunk {
        val result = mutableListOf<Byte>()
        bytes.forEach { byte ->
            result.add(mapping.getOrElse(byte, defaultValue = { throw IllegalArgumentException() }))
        }
        return DataChunk(result.toByteArray())
    }

    @ExperimentalStdlibApi
    fun writeEncodedLinesToFile(fileOut: String) {
        var consecutiveZeroPrints = 0
        var maxConsecutiveZeroPrints = 0
        var lastByteWasZero = false
        FileOutputStream(fileOut, true).use { writer ->
            encodedLines.toSortedMap(reverseOrder()).forEach { (_, bytes) ->
                if (DEBUG) {
                    bytes.forEach { byte ->
                        if (byte == 0.toByte()) {
                            if (lastByteWasZero) {
                                consecutiveZeroPrints++
                            } else {
                                consecutiveZeroPrints = 1
                                lastByteWasZero = true
                            }
                        } else {
                            maxConsecutiveZeroPrints = max(consecutiveZeroPrints, maxConsecutiveZeroPrints)
                            lastByteWasZero = false
                        }
                    }
                }

                writer.write(bytes)
            }

            if (maxConsecutiveZeroPrints > 0 && DEBUG) {
                log.warn("Chunk contained ecoded line with $maxConsecutiveZeroPrints consecutive 0x0000 byte(s).")
            }
        }
    }

    fun appendCurrentChunkToFile(fileOut: String) {
        FileOutputStream(fileOut, true).use { writer ->
            writer.write(bytes)
        }
    }
}

