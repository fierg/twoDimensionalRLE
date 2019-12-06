package edu.ba.twoDimensionalRLE.encoder

import de.jupf.staticlog.core.Logger
import edu.ba.twoDimensionalRLE.extensions.writeInvertedToBinaryStream
import loggersoft.kotlin.streams.BitStream
import kotlin.math.ceil
import kotlin.math.log2

@ExperimentalUnsignedTypes
interface Encoder {

    private val DEBUG: Boolean
        get() = true


    fun encode(inputFile: String, outputFile: String)
    fun decode(inputFile: String, outputFile: String)

     fun writeDecodedLengthHeaderToFile(count: Long, stream: BitStream, log: Logger): Long {
        log.info("Finding shortest header size...")
        val bytesNeeded = ceil(log2(count.toDouble() + 1) / 8)
        log.info("Write length header with $bytesNeeded bytes size.")
        var header = count.toString(2)
        val padSize = (bytesNeeded * 8).toInt()
        header = header.padStart(padSize, '0')

        StringBuffer(header).writeInvertedToBinaryStream(stream)
        stream.write(0.toByte())

        if (DEBUG) {
            stream.flush()
        }
        return (bytesNeeded + 1).toLong()
    }

     fun writeHuffmanMappingLengthToFile(
        huffmanMapping: Map<Byte, StringBuffer>,
        stream: BitStream,
        log: Logger
    ): Long {
        val bytesNeeded = ceil(log2(huffmanMapping.size.toDouble() + 1) / 8)

        log.info("Write mapping length header with $bytesNeeded bytes size.")

        var header = huffmanMapping.size.toString(2)
        val padSize = (bytesNeeded * 8).toInt()
        header = header.padStart(padSize, '0')

        StringBuffer(header).writeInvertedToBinaryStream(stream)
        stream.write(0.toByte())


        if (DEBUG) {
            stream.flush()
        }

        return (bytesNeeded).toLong() + 1
    }
     fun parseLengthHeader(stream: BitStream, log: Logger): Long {
        log.info("Trying to parse length header from encoded file...")
        var currentByteSize = 1
        var expectedSize = 0L
        stream.position++

        while (expectedSize == 0L) {
            val currentByte = stream.readByte()
            if (currentByte != 0.toByte()) {
                currentByteSize++
            } else {
                stream.position = 0
                expectedSize = stream.readBits(currentByteSize * 8, true)
            }
        }
        log.info("Expecting $expectedSize bytes of content after decoding.")
        return expectedSize + 2
    }
}