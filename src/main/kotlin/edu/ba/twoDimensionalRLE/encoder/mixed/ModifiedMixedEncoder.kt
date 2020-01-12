package edu.ba.twoDimensionalRLE.encoder.mixed

import de.jupf.staticlog.Log
import de.jupf.staticlog.core.LogLevel
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.Encoder
import edu.ba.twoDimensionalRLE.extensions.pow
import edu.ba.twoDimensionalRLE.tranformation.modified.BurrowsWheelerTransformationModified
import kanzi.SliceByteArray
import kanzi.transform.BWT
import kanzi.transform.BWTS
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
        val bwts = BWTS()
        var mappedFile: String? = null
        var transformedFile: String? = null

        analyzer.analyzeFile(File(inputFile))

        if (applyBurrowsWheelerTransformation) {
            log.debug("Applying bijective Burrows Wheeler Transformation to file...")
            transformedFile = "${outputFile}_bwt_tmp"
            val buf1 = File(inputFile).readBytes()
            val buf2 = ByteArray(buf1.size)
            val sa1 = SliceByteArray(buf1, 0)
            val sa2 = SliceByteArray(buf2, 0)
            bwts.forward(sa1,sa2)
            File(transformedFile).writeBytes(sa2.array)
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
                log.debug("Starting encoding of file...")

                for (bitPosition in 0..7) {
                    if (bitPosition > splitPosition) {
                        encodeBitPositionOfStreamRLE(bitsPerRLENumber1, bitPosition, streamIn, streamOut, lineMaps)
                    } else {
                        encodeBitPositionOfStreamRLE(bitsPerRLENumber2, bitPosition, streamIn, streamOut, lineMaps)

                    }
                }
            }
        }
        log.debug("Finished encoding.")
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}