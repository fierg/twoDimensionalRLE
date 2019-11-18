package edu.ba.twoDimensionalRLE.encoder.mixed

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.Encoder
import edu.ba.twoDimensionalRLE.encoder.huffman.HuffmanEncoder
import edu.ba.twoDimensionalRLE.encoder.rle.BinaryRunLengthEncoder
import edu.ba.twoDimensionalRLE.model.DataChunk
import edu.ba.twoDimensionalRLE.tranformation.BurrowsWheelerTrasformation
import java.io.File

class MixedEncoder : Encoder {


    private val log = Log.kotlinInstance()
    private val byteArraySize = 254
    private val btw = BurrowsWheelerTrasformation()
    private val analyzer = Analyzer()
    private val huffmanEncoder = HuffmanEncoder()
    private val binaryRunLengthEncoder = BinaryRunLengthEncoder()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    override fun encode(inputFile: String, outputFile: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun decode(inputFile: String, outputFile: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun encodeInternal(inputFile: String, outputFile: String) {
        analyzer.analyzeFile(File(inputFile))
        analyzer.addBWTSymbolsToMapping()

        val mapping = analyzer.getByteMapping()
        val chunks = DataChunk.readChunksFromFile(inputFile, byteArraySize, log)
        val transformedChunks = mutableListOf<DataChunk>()
        val mappedChunks = mutableListOf<DataChunk>()
        val encodedChunks = mutableListOf<DataChunk>()

        log.info("Performing burrows wheeler transformation on all chunks...")
        chunks.forEach {
            transformedChunks.add(btw.transformDataChunk(it))
        }

        log.info("Performing byte mapping to lower values on all chunks...")
        transformedChunks.forEach {
            mappedChunks.add(it.applyByteMapping(mapping))
        }

        log.info("Encoding all chunks...")
        mappedChunks.forEach {
            val encodedChunk = binaryRunLengthEncoder.encodeChunk(it, IntRange(5, 8))
            /*
            encodedChunk.encodedLines[3] = huffmanEncoder.encodeLine(it.bytes, 4)
            encodedChunk.encodedLines[3] = huffmanEncoder.encodeLine(it.bytes, 3)
            encodedChunk.encodedLines[3] = huffmanEncoder.encodeLine(it.bytes, 2)
            encodedChunk.encodedLines[3] = huffmanEncoder.encodeLine(it.bytes, 1)
            */
        }

    }
}