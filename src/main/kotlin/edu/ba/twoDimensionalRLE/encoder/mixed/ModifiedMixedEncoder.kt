package edu.ba.twoDimensionalRLE.encoder.mixed

import de.jupf.staticlog.Log
import de.jupf.staticlog.core.LogLevel
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.Encoder
import edu.ba.twoDimensionalRLE.extensions.pow
import edu.ba.twoDimensionalRLE.tranformation.bijectiveJavaWrapper.BWTSWrapper
import loggersoft.kotlin.streams.BitStream
import loggersoft.kotlin.streams.openBinaryStream
import java.io.File

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
class ModifiedMixedEncoder : Encoder {

    private val DEBUG = true
    private val log = Log.kotlinInstance()

    private val defaultZerosAfterHeadder = 2

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
        if (!DEBUG) log.logLevel = LogLevel.INFO

    }

    fun encodeInternal(
        inputFile: String,
        outputFile: String,
        bitsPerRLENumber1: Int,
        bitsPerRLENumber2: Int,
        applyByteMapping: Boolean,
        applyBurrowsWheelerTransformation: Boolean,
        splitPosition: Int
    ) {

        val analyzer = Analyzer()
        val bwts = BWTSWrapper()
        var mappedFile: String? = null
        var transformedFile: String? = null

        analyzer.analyzeFile(File(inputFile))

        if (applyBurrowsWheelerTransformation) {
            log.debug("Applying bijective Burrows Wheeler Transformation to file...")
            transformedFile = "${outputFile}_bwt_tmp"
            bwts.transformFile(File(inputFile), File(transformedFile))
        }

        if (applyByteMapping) {
            mappedFile = "${outputFile}_mapped_tmp"
            log.debug("Applying byte mapping to file...")
            analyzer.mapFile(if (applyBurrowsWheelerTransformation) transformedFile!! else inputFile, mappedFile)
        }

        var streamFile = inputFile
        if (applyBurrowsWheelerTransformation) streamFile = transformedFile!!
        if (applyByteMapping) streamFile = mappedFile!!


        BitStream(File(streamFile).openBinaryStream(true)).use { streamIn ->
            BitStream(File(outputFile).openBinaryStream(false)).use { streamOut ->
                if (applyByteMapping) {
                    log.debug("Writing mapping to file...")
                    writeLengthHeaderToFile(analyzer.getByteMapping().size, streamOut, log, 1)
                    writeByteMappingToStream(streamOut, analyzer.getByteMapping(), log)
                }
                val lineMaps = mutableMapOf<Int, MutableList<Int>>()
                for (i in 0..7) lineMaps[i] = mutableListOf()
                log.debug("Starting encoding of file...")

                for (bitPosition in 0..7) {
                    encodeBitPositionOfStreamRLE(
                        if (bitPosition > splitPosition) bitsPerRLENumber1 else bitsPerRLENumber2,
                        bitPosition,
                        streamIn,
                        streamOut,
                        lineMaps
                    )
                }

                streamOut.position = if (streamOut.offset != 0) streamOut.position + 1 else streamOut.position
                for (i in 0..1) streamOut.write(0.toByte())

                lineMaps.forEach { (t, u) ->
                    writeLengthHeaderToFile(u.count(), streamOut, log, if (t == 7) 0 else defaultZerosAfterHeadder)
                }
            }
        }
        log.debug("Finished encoding.")
    }

    fun decodeInternal(
        inputFile: String,
        outputFile: String,
        bitsPerRLENumber1: Int,
        bitsPerRLENumber2: Int,
        applyByteMapping: Boolean,
        applyBurrowsWheelerTransformation: Boolean,
        splitPosition: Int
    ) {
        val parsedNumbers = mutableListOf<Short>()
        BitStream(File(inputFile).openBinaryStream(true)).use { streamIn ->
            val countMap = parseCountMapFromTail(streamIn)
            val expectedMappingSize = parseCurrentHeader(streamIn, defaultZerosAfterHeadder, log)
            val mapping = parseByteMappingFromStream(streamIn, expectedMappingSize, log)

            BitStream(File(outputFile).openBinaryStream(false)).use { streamOut ->
                for (bitPosition in 0..7) {
                    decodeBitPositionOfEncodedStream(
                        if (bitPosition > splitPosition) bitsPerRLENumber1 else bitsPerRLENumber2,
                        bitPosition,
                        streamIn,
                        streamOut,
                        countMap
                    )
                }
            }
        }
    }

    private fun decodeBitPositionOfEncodedStream(
        bitsPerRLEencodedNumber: Int,
        bitPosition: Int,
        streamIn: BitStream,
        streamOut: BitStream,
        countMap: MutableMap<Int, Int>
    ) {
            

            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun parseCountMapFromTail(streamIn: BitStream): MutableMap<Int, Int> {
        log.debug("Trying to parse all number counts...")
        val countMap = mutableMapOf<Int, Int>()
        var counter = 0
        streamIn.position = streamIn.size - 1
        while (streamIn.position < streamIn.size && countMap.entries.size < 8) {
            countMap[counter++] = parseFromTail(streamIn, defaultZerosAfterHeadder, log)
        }
        streamIn.position = 0
        return countMap
    }

    private fun encodeBitPositionOfStreamRLE(
        bitsPerRLENumber: Int,
        bitPosition: Int,
        streamIn: BitStream,
        streamOut: BitStream,
        lineMaps: MutableMap<Int, MutableList<Int>>
    ) {
        var lastBit = false
        var currentBit: Boolean
        var counter = 0
        val maxLength = 2.pow(bitsPerRLENumber) - 1

        for (bytePosition in 0 until streamIn.size) {

            streamIn.position = bytePosition
            streamIn.offset = bitPosition

            currentBit = streamIn.readBit()

            if (lastBit == currentBit) {
                if (++counter == maxLength) {
                    writeRunToStream(counter, streamOut, bitsPerRLENumber)
                    lineMaps[bitPosition]!!.add(counter)
                    counter = 0
                }
            } else {
                if (counter > 0) {
                    writeRunToStream(counter, streamOut, bitsPerRLENumber)
                    lineMaps[bitPosition]!!.add(counter)
                    counter = 1
                } else {
                    counter++
                }
                lastBit = !lastBit
            }
        }
    }

    override fun encode(
        inputFile: String,
        outputFile: String,
        applyByteMapping: Boolean,
        applyBurrowsWheelerTransformation: Boolean,
        byteArraySize: Int,
        bitsPerRLENumber: Int
    ) {
        encodeInternal(
            inputFile,
            outputFile,
            5,
            bitsPerRLENumber,
            applyByteMapping,
            applyBurrowsWheelerTransformation,
            6
        )
    }

    override fun decode(
        inputFile: String,
        outputFile: String,
        applyByteMapping: Boolean,
        applyBurrowsWheelerTransformation: Boolean,
        byteArraySize: Int,
        bitsPerRLENumber: Int
    ) {
        decodeInternal(
            inputFile,
            outputFile,
            bitsPerRLENumber,
            bitsPerRLENumber,
            applyByteMapping,
            applyBurrowsWheelerTransformation,
            6
        )
    }
}