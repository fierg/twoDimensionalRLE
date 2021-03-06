package edu.ba.twoDimensionalRLE.encoder.mixed
/*
MIT License

Copyright (c) 2019 Sven Fiergolla

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

import de.jupf.staticlog.Log
import de.jupf.staticlog.core.LogLevel
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.Encoder
import edu.ba.twoDimensionalRLE.encoder.huffman.HuffmanEncoder
import edu.ba.twoDimensionalRLE.extensions.pow
import edu.ba.twoDimensionalRLE.tranformation.bijectiveJavaWrapper.BWTSWrapper
import loggersoft.kotlin.streams.BitStream
import loggersoft.kotlin.streams.openBinaryStream
import java.io.File

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
class ModifiedMixedEncoder(val DEBUGLOG: Boolean = false) : Encoder {

    private val DEBUG = false
    private val log = Log.kotlinInstance()

    private val defaultZerosAfterHeadder = 2

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
        if (!DEBUG) log.logLevel = LogLevel.INFO
        if (DEBUGLOG) log.logLevel = LogLevel.DEBUG
    }

    fun encodeInternal(
        inputFile: String,
        outputFile: String,
        bitsPerRLENumber: Int,
        bitsPerRLENumber2: Int,
        applyBurrowsWheelerTransformation: Boolean,
        applyByteMapping: Boolean,
        splitPosition: Int
    ) {
        val mapping = mutableMapOf<Int, Int>()
        for (i in 0..7) mapping[i] = if (i < splitPosition) bitsPerRLENumber2 else bitsPerRLENumber
        encodeInternal(inputFile, outputFile, applyByteMapping, applyBurrowsWheelerTransformation, false, mapping)
    }

    fun encodeInternal(
        inputFile: String,
        outputFile: String,
        applyByteMapping: Boolean,
        applyBurrowsWheelerTransformation: Boolean,
        applyHuffmanEncoding: Boolean,
        bitsPerRunMap: Map<Int, Int>
    ) {

        log.info("Encoding ${inputFile} with $bitsPerRunMap")

        if (applyByteMapping) log.info("Encoding with mapping as preprocessing.")
        if (applyBurrowsWheelerTransformation) log.info("Encoding with a Burrows Wheeler Transformation as preprocessing.")
        if (applyHuffmanEncoding) log.info("Encoding with huffman encoding instead of fixed size.")

        val analyzer = Analyzer(DEBUGLOG)
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
                    writeLengthHeaderToFile(analyzer.getByteMapping().size, streamOut, log, defaultZerosAfterHeadder)
                    writeByteMappingToStream(streamOut, analyzer.getByteMapping(), log)
                }
                val lineMaps = mutableMapOf<Int, MutableList<Int>>()
                for (i in 0..7) lineMaps[i] = mutableListOf()
                log.debug("Starting encoding of file...")

                for (bitPosition in 0..7) {
                    log.debug("Encoding all runs of bits of significance $bitPosition")
                    encodeBitPositionOfStreamRLE(
                        bitsPerRunMap.getOrElse(bitPosition) { throw IllegalArgumentException("No mapping found!") },
                        bitPosition,
                        streamIn,
                        streamOut,
                        lineMaps,
                        applyHuffmanEncoding
                    )
                }

                if (applyHuffmanEncoding) {
                    val huff = HuffmanEncoder(DEBUGLOG)
                    huff.encodeLineMapsToStream(lineMaps, streamOut, bitsPerRunMap.values.max()!!)
                }

                streamOut.position = if (streamOut.offset != 0) streamOut.position + 1 else streamOut.position
                for (i in 0..defaultZerosAfterHeadder) streamOut.write(0.toByte())

                lineMaps.toSortedMap(reverseOrder()).forEach { (t, u) ->
                    writeLengthHeaderToFile(u.count(), streamOut, log, if (t == 0) 0 else defaultZerosAfterHeadder)
                }
            }
        }
        log.info("Finished encoding.")
        if (applyByteMapping) File(mappedFile!!).delete()
        if (applyBurrowsWheelerTransformation) File(transformedFile!!).delete()
    }

    fun decodeInternal(
        inputFile: String,
        outputFile: String,
        bitsPerRLENumber1: Int,
        bitsPerRLENumber2: Int,
        applyByteMapping: Boolean,
        applyBurrowsWheelerTransformation: Boolean,
        applyHuffmanEncoding: Boolean? = false,
        splitPosition: Int
    ) {
        val mapping = mutableMapOf<Int, Int>()
        for (i in 0..7) mapping[i] = if (i < splitPosition) bitsPerRLENumber2 else bitsPerRLENumber1

        decodeInternal(
            inputFile,
            outputFile,
            applyByteMapping,
            applyBurrowsWheelerTransformation,
            applyHuffmanEncoding,
            mapping
        )
    }

    fun decodeInternal(
        inputFile: String,
        outputFile: String,
        applyByteMapping: Boolean,
        applyBurrowsWheelerTransformation: Boolean,
        applyHuffmanEncoding: Boolean?,
        bitsPerRLENumberMap: Map<Int, Int>
    ) {
        val analyzer = Analyzer(DEBUGLOG)
        val bwts = BWTSWrapper()

        var mapping = emptyMap<Byte, Byte>()
        val mappedFile = if (applyBurrowsWheelerTransformation) "${outputFile}_mapped_tmp" else outputFile
        val decodedFile =
            if (applyByteMapping || applyBurrowsWheelerTransformation) "${outputFile}_dec_tmp" else outputFile

        log.info("Decoding $inputFile")

        BitStream(File(inputFile).openBinaryStream(true)).use { streamIn ->
            val countMap = parseCountMapFromTail(streamIn)
            val totalExpectedRuns = countMap.map { it.value }.sum()
            if (applyByteMapping) {
                val expectedMappingSize = parseCurrentHeader(streamIn, defaultZerosAfterHeadder, log)
                mapping = parseByteMappingFromStream(streamIn, expectedMappingSize, log)
            }

            if (applyHuffmanEncoding == true) {
                val huffDecoder = HuffmanEncoder(DEBUGLOG)
                val expectedMappingSize = huffDecoder.parseCurrentHeader(streamIn, defaultZerosAfterHeadder, log)
                val huffmanMapping = huffDecoder.parseHuffmanMappingFromStream(streamIn, expectedMappingSize, log)
                val decodedRuns =
                    huffDecoder.decodeIntListFromStream(huffmanMapping, streamIn, countMap.map { it.value }.sum())
                val runMap = mutableMapOf<Int, List<Int>>()
                var listOffset = 0
                for (i in 0..7) {
                    runMap[i] = decodedRuns.subList(listOffset, listOffset + countMap[i]!!)
                    listOffset += countMap[i]!!
                }

                assert(
                    decodedRuns.size == totalExpectedRuns,
                    lazyMessage = { "Unexpected number of runs decoded! expected $totalExpectedRuns, found ${decodedRuns.size}" })

                BitStream(File(decodedFile).openBinaryStream(false)).use { streamOut ->
                    log.debug("decoding all runs to file...")
                    decodeBitPositionOfRunMap(streamOut, runMap, log)
                }

            } else {

                BitStream(File(decodedFile).openBinaryStream(false)).use { streamOut ->
                    for (bitPosition in 0..7) {
                        log.debug("decoding all runs for bits of significance $bitPosition...")
                        decodeBitPositionOfEncodedStream(
                            bitsPerRLENumberMap.getOrElse(bitPosition) { throw IllegalArgumentException("No mapping found!") },
                            bitPosition,
                            streamIn,
                            streamOut,
                            countMap.getOrElse(bitPosition) { throw IllegalArgumentException("No count mapping found for bit position $bitPosition!") }
                        )
                    }
                }
            }

            if (applyByteMapping) {
                log.debug("Applying byte mapping to file...")
                analyzer.mapFile(decodedFile, mappedFile, mapping)
            }

            if (applyBurrowsWheelerTransformation) {
                log.debug("Applying bijective Burrows Wheeler Transformation to file...")
                bwts.invert(File(if (applyByteMapping) mappedFile else decodedFile), File(outputFile))
            }
            if (applyBurrowsWheelerTransformation xor applyByteMapping)
                File(decodedFile).delete()
            if (applyBurrowsWheelerTransformation && applyByteMapping) {
                File(decodedFile).delete()
                File(mappedFile).delete()
            }
        }
    }

    private fun decodeBitPositionOfEncodedStream(
        bitsPerRLEencodedNumber: Int,
        bitPosition: Int,
        streamIn: BitStream,
        streamOut: BitStream,
        expectedCount: Int
    ) {
        streamOut.position = 0
        var currentBit = false
        var currentCount = 0
        var currentNumber: Int

        while (currentCount < expectedCount) {
            currentNumber = streamIn.readUBits(bitsPerRLEencodedNumber).toInt()
            if (currentCount + 1 == expectedCount) {
                log.debug("last number parsed: $currentNumber")
            }
            for (i in 0 until currentNumber) {
                streamOut.offset = bitPosition
                if (currentBit) streamOut += currentBit
                if (!currentBit || bitPosition != 7) streamOut.position++
            }
            currentCount++
            currentBit = !currentBit
        }
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
        lineMaps: MutableMap<Int, MutableList<Int>>,
        applyHuffmanEncoding: Boolean
    ) {
        var lastBit = false
        var currentBit: Boolean
        var counter = 0
        val maxLength = 2.pow(bitsPerRLENumber) - 1

        for (bytePosition in 0 until streamIn.size) {

            streamIn.position = bytePosition
            streamIn.offset = bitPosition

            currentBit = streamIn.readBit()
            //log.debug("bit at position 0x${Integer.toHexString(streamIn.position.toInt())} : ${streamIn.offset} equals ${if (currentBit) 1 else 0} ")

            if (lastBit == currentBit) {
                counter++
                if (counter > maxLength) {
                    if (!applyHuffmanEncoding) {
                        writeRunToStream(counter - 1, streamOut, bitsPerRLENumber)
                        writeRunToStream(0, streamOut, bitsPerRLENumber)
                    }
                    lineMaps[bitPosition]!!.add(counter - 1)
                    lineMaps[bitPosition]!!.add(0)
                    counter = 1
                }
            } else {
                if (!applyHuffmanEncoding) writeRunToStream(counter, streamOut, bitsPerRLENumber)
                lineMaps[bitPosition]!!.add(counter)
                lastBit = !lastBit
                counter = 1
            }
        }
        if (counter != 0) {
            if (!applyHuffmanEncoding) writeRunToStream(counter, streamOut, bitsPerRLENumber)
            lineMaps[bitPosition]!!.add(counter)
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
            false,
            5
        )
    }
}