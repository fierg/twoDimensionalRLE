package edu.ba.twoDimensionalRLE.encoder.mixed

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.Encoder
import edu.ba.twoDimensionalRLE.encoder.huffman.HuffmanEncoder
import edu.ba.twoDimensionalRLE.model.DataChunk
import edu.ba.twoDimensionalRLE.tranformation.BurrowsWheelerTrasformation
import java.io.File

class MixedEncoder : Encoder {


    private val log = Log.kotlinInstance()
    private val byteArraySize = 254
    private val btw = BurrowsWheelerTrasformation()
    private val analyzer = Analyzer()
    private val huffmanEncoder = HuffmanEncoder()

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


        chunks.forEach {
            transformedChunks.add(btw.transformDataChunk(it))
        }

        transformedChunks.forEach {
            mappedChunks.add(it.applyByteMapping(mapping))
        }

        mappedChunks.forEach {
            huffmanEncoder.encodeLine(it.bytes, 0)
        }

    }
}