package edu.ba.twoDimensionalRLE.encoder.mixed

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.Encoder
import edu.ba.twoDimensionalRLE.encoder.huffman.HuffmanEncoder
import edu.ba.twoDimensionalRLE.encoder.rle.BinaryRunLengthEncoder
import edu.ba.twoDimensionalRLE.model.DataChunk
import edu.ba.twoDimensionalRLE.tranformation.BurrowsWheelerTransformation
import java.io.File

class MixedEncoder : Encoder {

    private val byteArraySize = 254
    private val bwt = BurrowsWheelerTransformation()
    private val analyzer = Analyzer()
    private val binaryRunLengthEncoder = BinaryRunLengthEncoder()
    private val DEBUG = true
    private val byteSize = 8
    private val bitsPerRLENumber = 4
    private val log = Log.kotlinInstance()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    @ExperimentalStdlibApi
    override fun encode(inputFile: String, outputFile: String) {
        encodeInternal(inputFile, outputFile)
    }

    override fun decode(inputFile: String, outputFile: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ExperimentalStdlibApi
    private fun encodeInternal(inputFile: String, outputFile: String) {
        analyzer.analyzeFile(File(inputFile))
        analyzer.addBWTSymbolsToMapping()
        val huffmanEncoder = HuffmanEncoder()

        val mapping = analyzer.getByteMapping()
        val chunks = DataChunk.readChunksFromFile(inputFile, byteArraySize, log)
        val transformedChunks = mutableListOf<DataChunk>()
        val mappedChunks = mutableListOf<DataChunk>()
        val encodedChunks = mutableListOf<DataChunk>()

        log.info("Performing burrows wheeler transformation on all chunks, adding 2 Byte...")
        chunks.forEach {
            transformedChunks.add(bwt.transformDataChunk(it))
        }
        log.info("Finished burrows wheeler transformation.")

        if (DEBUG) {
            transformedChunks.stream().forEach { it.writeCurrentChunk(outputFile + "_transformed") }
        }

        log.info("Performing byte mapping to lower values on all chunks...")
        transformedChunks.forEach {
            mappedChunks.add(it.applyByteMapping(mapping))
        }
        log.info("Finished byte mapping.")

        if (DEBUG) {
            mappedChunks.stream().forEach { it.writeCurrentChunk(outputFile + "_mapped") }
        }

        log.info("Encoding all chunks with binary RLE and Huffman Encoding in parallel...")
        mappedChunks.forEach {
            var encodedChunk = binaryRunLengthEncoder.encodeChunk(it, IntRange(5, 8), bitsPerRLENumber, byteSize)
            encodedChunk = huffmanEncoder.encodeChunk(encodedChunk, IntRange(1, 4), bitsPerRLENumber, byteSize)
            encodedChunks.add(encodedChunk)
        }
        log.info("Finished encoding.")

        log.info("Writing all encoded lines of all chunks to file...")
        encodedChunks.stream().forEach { it.writeEncodedLinesToFile(outputFile, bitsPerRLENumber) }
        log.info("Finished encoding.")
    }

    @ExperimentalUnsignedTypes
    fun decodeInternal(inputFile: String, outputFile: String) {
        val encodedChunks = DataChunk.readChunksFromEncodedFile(inputFile, byteArraySize, log)
        val mappedChunks = mutableListOf<DataChunk>()
        val transformedChunks = mutableListOf<DataChunk>()
        val decodedChunks = mutableListOf<DataChunk>()


    }
}