package edu.ba.twoDimensionalRLE.other

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.extensions.reversed
import edu.ba.twoDimensionalRLE.model.DataChunk
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File

@ExperimentalUnsignedTypes
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class MappingTest {

    private var log = Log.kotlinInstance()
    private val DEBUG = true

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    companion object {
        private const val fileToEncodeSmall = "testFile_small.txt"
        private const val fileToEncode = "t8.shakespeare.txt"
        private const val encodeFolder = "data/encoded/mapping"
        private const val decodeFolder = "data/decoded/mapping"
    }

    @Test
    @Order(1)
    fun cleanup() {
        if (File(encodeFolder).exists()) {
            File(encodeFolder).deleteRecursively()
            File(decodeFolder).deleteRecursively()
        }
        File(encodeFolder).mkdirs()
        File(decodeFolder).mkdirs()
    }

    @Test
    @Order(2)
    fun applyAndRevertMappingSmall() {
        val analyzer = Analyzer()

        log.info("Reading file and generating byte mapping...")
        analyzer.analyzeFile(File("data/$fileToEncodeSmall"))
        val chunks = DataChunk.readChunksFromFile("data/$fileToEncodeSmall", 256, log)
        val mappedChunks = mutableListOf<DataChunk>()
        val reMappedChunks = mutableListOf<DataChunk>()

        log.info("Apply mapping to all chunks..")
        chunks.forEach { chunk ->
            mappedChunks.add(chunk.applyByteMapping(analyzer.getByteMapping()))
        }
        log.info("Finished mapping.")

        if (DEBUG) {
            log.debug("Writing mapped chunks to $encodeFolder/${fileToEncodeSmall}_mapped")
            mappedChunks.stream().forEach { it.appendCurrentChunkToFile("$encodeFolder/${fileToEncodeSmall}_mapped") }
        }

        log.info("Invert mapping of all chunks...")
        val reversedMapping = analyzer.getByteMapping().reversed()
        mappedChunks.forEach {
            reMappedChunks.add(it.applyByteMapping(reversedMapping))
        }

        if (DEBUG) {
            log.debug("Writing mapped chunks to $decodeFolder/$fileToEncodeSmall")
            reMappedChunks.stream().forEach { it.appendCurrentChunkToFile("$decodeFolder/$fileToEncodeSmall") }
        }

        log.info("Validating equality of input and output...")
        chunks.forEachIndexed { index, chunk ->
            assert(reMappedChunks[index].bytes.contentEquals(chunk.bytes))
        }
        log.info("Validation succeeded.")
    }

    @Test
    @Order(3)
    fun applyAndRevertMappingLarge() {
        val analyzer = Analyzer()

        log.info("Reading file and generating byte mapping...")
        analyzer.analyzeFile(File("data/$fileToEncode"))
        val chunks = DataChunk.readChunksFromFile("data/$fileToEncode", 256, log)
        val mappedChunks = mutableListOf<DataChunk>()
        val reMappedChunks = mutableListOf<DataChunk>()

        log.info("Apply mapping to all chunks..")
        chunks.forEach { chunk ->
            mappedChunks.add(chunk.applyByteMapping(analyzer.getByteMapping()))
        }
        log.info("Finished mapping.")

        if (DEBUG) {
            log.debug("Writing mapped chunks to $encodeFolder/${fileToEncode}_mapped")
            mappedChunks.stream().forEach { it.appendCurrentChunkToFile("$encodeFolder/${fileToEncode}_mapped") }
        }

        log.info("Invert mapping of all chunks...")
        val reversedMapping = analyzer.getByteMapping().reversed()
        mappedChunks.forEach {
            reMappedChunks.add(it.applyByteMapping(reversedMapping))
        }
        if (DEBUG) {
            log.debug("Writing remapped chunks to $decodeFolder/$fileToEncode")
            reMappedChunks.stream().forEach { it.appendCurrentChunkToFile("$decodeFolder/$fileToEncode") }
        }

        log.info("Validating equality of input and output...")
        chunks.forEachIndexed { index, chunk ->
            assert(reMappedChunks[index].bytes.contentEquals(chunk.bytes))
        }
        log.info("Validation succeeded.")
    }
}