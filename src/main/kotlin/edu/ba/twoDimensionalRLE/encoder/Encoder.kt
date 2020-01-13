package edu.ba.twoDimensionalRLE.encoder

import de.jupf.staticlog.core.Logger
import edu.ba.twoDimensionalRLE.extensions.writeInvertedToBinaryStream
import loggersoft.kotlin.streams.BitStream
import kotlin.math.ceil
import kotlin.math.log2

@ExperimentalUnsignedTypes
interface Encoder {

    private val DEBUG: Boolean
        get() = false


    fun encode(
        inputFile: String,
        outputFile: String,
        applyByteMapping: Boolean,
        applyBurrowsWheelerTransformation: Boolean, byteArraySize: Int, bitsPerRLENumber: Int
    )

    fun decode(
        inputFile: String,
        outputFile: String,
        applyByteMapping: Boolean,
        applyBurrowsWheelerTransformation: Boolean, byteArraySize: Int, bitsPerRLENumber: Int
    )

    fun writeDecodedLengthHeaderToFile(count: Long, stream: BitStream, log: Logger): Long {
        val bytesNeeded = writeCountToStream(log, count.toInt(), stream)
        stream.write(0.toByte())

        if (DEBUG) {
            stream.flush()
        }
        return (bytesNeeded + 1).toLong()
    }

    fun writeLengthHeaderToFile(count: Int, stream: BitStream, log: Logger, numberOfZerosAfter: Int): Long {
        val bytesNeeded = writeCountToStream(log, count, stream)
        for (i in 0 until numberOfZerosAfter) {
            stream.write(0.toByte())
        }

        if (DEBUG) {
            stream.flush()
        }
        return (bytesNeeded + numberOfZerosAfter).toLong()
    }

    private fun writeCountToStream(
        log: Logger,
        count: Int,
        stream: BitStream
    ): Double {
        log.debug("Finding shortest header size for count $count...")
        val bytesNeeded = ceil(log2(count.toDouble() + 1) / 8)
        var header = count.toString(2)
        val padSize = (bytesNeeded * 8).toInt()
        header = header.padStart(padSize, '0')

        log.debug("Write length header with ${bytesNeeded.toInt()} bytes size.")
        StringBuffer(header).writeInvertedToBinaryStream(stream)
        log.debug("Wrote $count as  0x${Integer.toHexString(count)} / $header so stream.")
        if (DEBUG) stream.flush()
        return bytesNeeded
    }

    fun writeHuffmanMappingLengthToFile(
        huffmanMapping: Map<Byte, StringBuffer>,
        stream: BitStream,
        log: Logger
    ): Long {
        val bytesNeeded = ceil(log2(huffmanMapping.size.toDouble() + 1) / 8)

        log.debug("Write mapping length header with ${bytesNeeded.toInt()} bytes size.")

        var header = huffmanMapping.size.toString(2)
        val padSize = (bytesNeeded * 8).toInt()
        header = header.padStart(padSize, '0')

        StringBuffer(header).writeInvertedToBinaryStream(stream)
        log.debug("Wrote $header so stream.")

        stream.write(0.toByte())


        if (DEBUG) {
            stream.flush()
        }

        return (bytesNeeded).toLong() + 1
    }

    fun parseLengthHeader(stream: BitStream, log: Logger): Long {
        log.debug("Trying to parse length header from encoded file...")
        var currentByteSize = 1
        var expectedSize = 0L
        stream.position++

        while (expectedSize == 0L) {
            val currentByte = stream.readByte()
            if (currentByte != 0.toByte()) {
                currentByteSize++
            } else {
                stream.position = 0
                expectedSize = stream.readBits(currentByteSize * 8, false)
            }
        }
        log.debug("Expecting $expectedSize bytes of content after decoding.")
        return expectedSize
    }

    fun parseCurrentHeader(stream: BitStream, numberOfZerosAfter: Int, log: Logger): Int {
        var currentByteSize = 0
        var zerosRead = 0
        var bytesRead = 0
        var expectedSize = 0L

        while (expectedSize == 0L) {
            val currentByte = stream.readByte()
            log.debug(
                "current byte read at 0x${Integer.toHexString(stream.position.toInt()).padStart(
                    2,
                    '0'
                )}, position ${stream.position - 1}: ${Integer.toHexString(currentByte.toUByte().toInt())}"
            )
            bytesRead++
            if (currentByte != 0.toByte()) {
                currentByteSize++
                zerosRead = 0
            } else {
                zerosRead++
                if (zerosRead == numberOfZerosAfter) {
                    stream.position = stream.position - bytesRead
                    expectedSize = stream.readBits(currentByteSize * 8, false)
                    stream.position++
                } else {
                    currentByteSize++
                }
            }
        }
        log.debug("Parsed 0x${Integer.toHexString(expectedSize.toInt())} -> a size of $expectedSize.")
        assert(Int.MAX_VALUE > expectedSize, lazyMessage = { "Casting overflow while parsing a header!" })
        return expectedSize.toInt()
    }

    fun parseFromTail(stream: BitStream, numberOfZerosAfter: Int, log: Logger): Int {
        var currentByteSize = 0
        var zerosRead = 0
        var bytesRead = 0
        var expectedSize = 0L

        while (expectedSize == 0L) {
            val currentByte = stream.readByte()
            stream.position = stream.position - 2
            log.debug(
                "current byte read at 0x${Integer.toHexString(stream.position.toInt()).padStart(
                    2,
                    '0'
                )}, position ${stream.position}: ${Integer.toHexString(currentByte.toUByte().toInt())}"
            )

            bytesRead++
            if (currentByte != 0.toByte()) {
                currentByteSize++
                zerosRead = 0
            } else {
                zerosRead++
                if (zerosRead == numberOfZerosAfter) {
                    stream.position = stream.position + numberOfZerosAfter + 1
                    expectedSize = stream.readBits((currentByteSize - 1) * 8, false)
                    stream.position = stream.position - (currentByteSize - 1) - numberOfZerosAfter - 1
                    log.debug("new stream position: ${stream.position}")
                } else {
                    currentByteSize++
                }
            }
        }
        log.debug("Parsed 0x${Integer.toHexString(expectedSize.toInt())} -> a size of $expectedSize.")
        assert(Int.MAX_VALUE > expectedSize, lazyMessage = { "Casting overflow while parsing a header!" })
        return expectedSize.toInt()
    }

    fun parseHuffmanMappingFromStream(
        stream: BitStream,
        expectedMappingSize: Int,
        log: Logger
    ): Map<StringBuffer, Byte> {

        log.debug("Trying to parse $expectedMappingSize mappings from encoded file...")
        val huffmanMapping = mutableMapOf<StringBuffer, Byte>()

        while (stream.position < stream.size && huffmanMapping.size < expectedMappingSize) {
            val currentPrefix = StringBuffer()
            val byteToMap = stream.readByte()
            val prefixLength = stream.readBits(8, false)
            for (position in 0 until prefixLength) {
                if (stream.readBit()) currentPrefix.append('1') else currentPrefix.append('0')
            }
            huffmanMapping[currentPrefix] = byteToMap
        }
        log.debug("Parsed ${huffmanMapping.size} mappings from encoded file.")
        log.debug("Huffman dictionary found: $huffmanMapping")
        return huffmanMapping
    }

    fun writeByteMappingToStream(stream: BitStream, mapping: Map<Byte, Byte>, log: Logger) {
        log.debug("Writing byte mapping to stream...")
        mapping.keys.forEach {
            stream.write(it)
        }

        if (DEBUG) stream.flush()
    }

    fun parseByteMappingFromStream(stream: BitStream, expectedMappingSize: Int, log: Logger): Map<Byte, Byte> {
        log.debug("Expecting $expectedMappingSize mappings from stream...")
        val mapping = mutableMapOf<Byte, Byte>()
        var mappingCounter = 0
        while (stream.position < stream.size && mapping.size < expectedMappingSize) {
            val currentByte = stream.readByte()
            mapping[mappingCounter++.toByte()] = currentByte
        }
        log.debug("Mapping found: $mapping")
        return mapping
    }

    fun writeRunToStream(counter: Int, stream: BitStream, bitPerRun: Int) {
        counter.toString(2).padStart(bitPerRun, '0').reversed().forEach {
            stream += when (it) {
                '0' -> false
                '1' -> true
                else -> throw IllegalArgumentException()
            }
        }
    }

}