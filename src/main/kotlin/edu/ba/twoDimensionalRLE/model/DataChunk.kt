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

@ExperimentalUnsignedTypes
open class DataChunk(val input: ByteArray) {

    private val log = Log.kotlinInstance()
    val encodedLines = mutableMapOf<Int, ByteArray>()
    val decodedLines = mutableMapOf<Int, ByteArray>()
    var huffEncodedStringBuffer = StringBuffer()
    var binRleEncodedNumbers = mutableListOf<Int>()
    val rleEncodedBytes = mutableListOf<Byte>()
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

    fun applyByteMapping(mapping: Map<Byte, Byte>): DataChunk {
        val result = mutableListOf<Byte>()
        bytes.forEach { byte ->
            result.add(mapping.getOrElse(byte, defaultValue = { throw IllegalArgumentException() }))
        }
        return DataChunk(result.toByteArray())
    }


    @Deprecated("encode continuously!")
    fun writeEncodedLinesToFile(fileOut: String) {
        FileOutputStream(fileOut, true).use { writer ->
            encodedLines.toSortedMap(reverseOrder()).forEach { (_, bytes) ->
                writer.write(bytes)
            }
        }
    }

    fun appendCurrentChunkToFile(fileOut: String) {
        FileOutputStream(fileOut, true).use { writer ->
            writer.write(bytes)
        }
    }
}

