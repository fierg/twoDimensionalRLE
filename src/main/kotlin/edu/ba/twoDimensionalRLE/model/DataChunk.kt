package edu.ba.twoDimensionalRLE.model

import de.jupf.staticlog.core.Logger
import edu.ba.twoDimensionalRLE.extensions.isWholeNumber
import edu.ba.twoDimensionalRLE.extensions.pow
import edu.ba.twoDimensionalRLE.extensions.toBitSet
import edu.ba.twoDimensionalRLE.extensions.toBitSetList
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import kotlin.experimental.and
import kotlin.experimental.or

class DataChunk(val bytes: ByteArray) {

    val encodedLines = mutableMapOf<Int, ByteArray>()

    companion object {
        fun readChunksFromFile(inputFile: String, byteArraySize: Int, log: Logger): MutableList<DataChunk> {
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
            return chunks
        }

        fun readChunksFromEncodedFile(inputFile: String, byteArraySize: Int, log: Logger): List<DataChunk> {
            val input = File(inputFile)
            val bytes = ByteArray(byteArraySize)
            val chunks = mutableListOf<DataChunk>()
            var counter = 0
            log.info("Reading $input into chunks of size $byteArraySize bytes...")

            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    fun getLineFromChunk(line: Int, bitSize: Int): ByteArray {
        assert(bitSize >= line)
        val noOfChars = bytes.size / bitSize.toDouble()
        assert(noOfChars.isWholeNumber())

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

    fun writeEncodedLinesToFile(fileOut: String, bitsPerSymbol: Int) {
        FileOutputStream(fileOut, true).use { writer ->
            encodedLines.forEach { (i, bytes) ->
                writer.write(bytes)
            }
            writer.write(writeLineEnding(bitsPerSymbol))
        }
    }

    private fun writeLineEnding(bitsPerSymbol: Int): ByteArray {
        val sb = StringBuffer()
        sb.append("0".padStart(bitsPerSymbol - 1, '0'))
        return sb.toBitSet().toByteArray()
    }


}

