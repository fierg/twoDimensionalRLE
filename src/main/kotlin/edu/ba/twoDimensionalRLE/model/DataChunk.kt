package edu.ba.twoDimensionalRLE.model

import de.jupf.staticlog.core.Logger
import edu.ba.twoDimensionalRLE.extensions.isWholeNumber
import edu.ba.twoDimensionalRLE.extensions.pow
import edu.ba.twoDimensionalRLE.extensions.toBinStringBuffer
import java.io.File
import java.io.FileOutputStream
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.ceil
import kotlin.math.log

@ExperimentalUnsignedTypes
open class DataChunk(val input: ByteArray) {

    val encodedLines = mutableMapOf<Int, ByteArray>()
    val decodedLines = mutableMapOf<Int, ByteArray>()
    val decodedLinesStrBuffer = mutableMapOf<Int, String>()
    var huffEncodedStringBuffer = StringBuffer()
    var huffEncodedBytes = 0
    var binRleEncodedNumbers = mutableListOf<Int>()
    var binRleEncodedNumbersFromBuffer = mutableListOf<Int>()
    var bytesToEncodeWithHuffman = ByteArray(0)
    val rleEncodedBytes = mutableListOf<Byte>()
    val bytes = input.clone()


    companion object {
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

        internal fun readChunksFromDecodedParts(
            totalSize: Int,
            byteArraySize: Int,
            binRleRange: IntRange,
            rleRange: IntRange,
            huffRange: IntRange,
            huffDecodedBytes: ByteArray,
            binRleBuffer: StringBuffer,
            log: Logger
        ): List<DataChunk> {
            log.info("Starting to reconstruct DataChunks from parsed buffers...")
            val result = mutableListOf<DataChunk>()
            var currentChunk = DataChunk(ByteArray(0))
            var remainingSize = totalSize
            val remainingBinRleBuffer = StringBuffer(binRleBuffer)

            log.info("Building buffer from bytes...")
            val remainingHuffmanBuffer = huffDecodedBytes.toBinStringBuffer()
            var currentLength = 1

            try {
                while (currentLength > 0) {
                    currentLength = if (remainingSize >= byteArraySize) {
                        remainingSize -= byteArraySize
                        byteArraySize
                    } else {
                        remainingSize
                    }

                    //build bin rle decoded lines
                    binRleRange.forEach { line ->
                        currentChunk.decodedLinesStrBuffer[line] = remainingBinRleBuffer.substring(0, currentLength)
                        remainingBinRleBuffer.delete(0, currentLength)
                    }

                    //huff decoded lines
                    huffRange.forEach { line ->
                        currentChunk.decodedLinesStrBuffer[line] = remainingHuffmanBuffer.substring(0, currentLength)
                        remainingHuffmanBuffer.delete(0, currentLength)
                    }

                    result.add(currentChunk)
                    currentChunk = DataChunk(ByteArray(0))

                    if (result.size % 1000 == 0){
                        log.info("reconstructed lines of chunk number ${result.size}")
                    }
                }
            } catch (e: Exception) {
                //TODO check if really no mor data is left
                log.debug("Unable to parse further chunks!")
            }

            log.info("Finished reconstructing all lines in all chunks.")
            return result
        }

    }

    fun getLineFromChunk(line: Int, lineSizeInNrOfByteArrays: Int): ByteArray {
        assert(lineSizeInNrOfByteArrays >= line)
        var noOfChars = bytes.size / lineSizeInNrOfByteArrays.toDouble()
        if (!noOfChars.isWholeNumber()) {
            noOfChars = ceil(noOfChars)
        }

        val chars = ByteArray(noOfChars.toInt())
        var byteCounter = 0
        bytes.forEachIndexed { index, byte ->
            if (index != 0 && index % lineSizeInNrOfByteArrays == 0)
                byteCounter++
            if (byte.and(2.pow(line).toByte()) == 2.pow(line).toByte())
                chars[byteCounter] = chars[byteCounter].or(2.pow(index % lineSizeInNrOfByteArrays).toByte())
        }
        return chars
    }

    fun getLineFromChunkAsStringBuffer(line: Int): StringBuffer {
        val bits = StringBuffer()
        bytes.forEach { byte ->
            if (byte.toInt().shl(7 - line).toUByte().toInt().shr(7) == 1) {
                bits.append('1')
            } else {
                bits.append('0')
            }
        }
        return bits
    }

    fun applyByteMapping(mapping: Map<Byte, Byte>): DataChunk {
        val result = mutableListOf<Byte>()
        bytes.forEach { byte ->
            result.add(
                mapping.getOrElse(
                    byte,
                    defaultValue = { throw IllegalArgumentException("Trying to apply byte mapping to unknown byte $byte.") })
            )
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

