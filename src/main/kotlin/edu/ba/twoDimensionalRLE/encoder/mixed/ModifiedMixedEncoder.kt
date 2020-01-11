package edu.ba.twoDimensionalRLE.encoder.mixed

import de.jupf.staticlog.Log
import de.jupf.staticlog.core.LogLevel
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.Encoder
import edu.ba.twoDimensionalRLE.extensions.pow
import edu.ba.twoDimensionalRLE.tranformation.BurrowsWheelerTransformationModified
import loggersoft.kotlin.streams.BitStream
import loggersoft.kotlin.streams.openBinaryStream
import java.io.File

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
class ModifiedMixedEncoder : Encoder {

    private val DEBUG = false

    private val log = Log.kotlinInstance()

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
        val bwt = BurrowsWheelerTransformationModified()
        var mappedFile: String? = null
        var transformedFile: String? = null

        analyzer.analyzeFile(File(inputFile))

        if(applyBurrowsWheelerTransformation){
            mappedFile = "${outputFile}_bwt_tmp"

        }

        if (applyByteMapping) {
            mappedFile = "${outputFile}_mapped_tmp"
            analyzer.mapFile(inputFile, mappedFile)
        }

        BitStream(File(mappedFile ?: inputFile).openBinaryStream(true)).use { streamIn ->
            BitStream(File(outputFile).openBinaryStream(false)).use { streamOut ->

                if (applyByteMapping) {
                    writeLengthHeaderToFile(analyzer.getByteMapping().size,streamOut,log,1)
                    writeByteMappingToStream(streamOut, analyzer.getByteMapping(), log)
                }

                val lineMaps = mutableMapOf<Int, MutableList<Int>>()

                for (bitPosition in 0..7) {
                    if (bitPosition > splitPosition) {
                        encodeBitPositionOfStreamRLE(bitsPerRLENumber1, bitPosition, streamIn, streamOut, lineMaps)
                    } else {
                        encodeBitPositionOfStreamRLE(bitsPerRLENumber2, bitPosition, streamIn, streamOut, lineMaps)

                    }
                }
            }
        }
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
                    if (DEBUG) lineMaps.getOrElse(bitPosition, defaultValue = { mutableListOf() }).add(
                        counter
                    )
                    counter = 0
                }
            } else {
                if (counter > 0) {
                    writeRunToStream(counter, streamOut, bitsPerRLENumber)
                    if (DEBUG) lineMaps.getOrElse(bitPosition, defaultValue = { mutableListOf() }).add(
                        counter
                    )
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
        encodeInternal(inputFile, outputFile, 5, bitsPerRLENumber, applyByteMapping, applyBurrowsWheelerTransformation,6)
    }

    override fun decode(
        inputFile: String,
        outputFile: String,
        applyByteMapping: Boolean,
        applyBurrowsWheelerTransformation: Boolean,
        byteArraySize: Int,
        bitsPerRLENumber: Int
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}