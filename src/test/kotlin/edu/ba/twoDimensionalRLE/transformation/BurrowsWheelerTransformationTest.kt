package edu.ba.twoDimensionalRLE.transformation

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.model.DataChunk
import edu.ba.twoDimensionalRLE.tranformation.BurrowsWheelerTransformation
import edu.ba.twoDimensionalRLE.tranformation.bijective.BurrowsWheelerTransformationS
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File
import kotlin.test.assertFailsWith

@ExperimentalUnsignedTypes
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class BurrowsWheelerTransformationTest {

    private var log = Log.kotlinInstance()
    private val BWTS = BurrowsWheelerTransformationS()
    private val byteArraySize = 126
    private val DEBUG = true

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    companion object {
        private const val fileToEncodeSmall = "testFile_small.txt"
        private const val fileToEncode = "t8.shakespeare_medium.txt"
        private const val encodeFolder = "data/encoded/bwt"
        private const val decodeFolder = "data/decoded/bwt"
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
    fun simpleTest() {
        val tests = listOf(
            "banana",
            "hello this is a normal sentence.",
            "this is a sentence ii with iiii many iiiiis"
        )

        val transformer = BurrowsWheelerTransformation()

        for (test in tests) {
            println(transformer.makePrintable(test))
            print(" --> ")
            var encoded = ""
            try {
                encoded = transformer.transform(test)
                println(transformer.makePrintable(encoded))
            } catch (ex: RuntimeException) {
                println("ERROR: " + ex.message)
            }
            val decoded = transformer.invertTransform(encoded)

            assert(decoded == test)
            println(" --> $decoded\n")
        }
    }

    @Test
    @Order(3)
    fun simpleTestFail() {
        val tests = listOf("\u0002ABC\u0003")

        val transformer = BurrowsWheelerTransformation()

        assertFailsWith<RuntimeException> {
            for (test in tests) {
                println(transformer.makePrintable(test))
                print(" --> ")
                val encoded = transformer.transform(test)
                println(transformer.makePrintable(encoded))
            }
        }.message?.let { log.error(it) }
    }

    @ExperimentalStdlibApi
    @Test
    @Order(4)
    fun transformChunk() {
        val chunks = DataChunk.readChunksFromFile("data/$fileToEncodeSmall", byteArraySize, log)
        val transformedChunks = mutableListOf<DataChunk>()
        val reversedChunks = mutableListOf<DataChunk>()
        val bwt = BurrowsWheelerTransformation()

        log.info("Performing burrows wheeler transformation on all chunks, adding 2 Byte...")
        chunks.forEach {
            transformedChunks.add(bwt.transformDataChunk(it))
        }

        log.info("Finished burrows wheeler transformation.")
        log.info("Performing inverse burrows wheeler transformation on all chunks...")

        transformedChunks.forEach {
            reversedChunks.add(bwt.invertTransformDataChunk(it))
        }
        log.info("Finished inverse transformation on all ${chunks.size} chunks.")

        log.info("input:")
        chunks.forEachIndexed { index, chunk -> log.info("Chunk nr $index: \n" + chunk.bytes.decodeToString()) }

        log.info("transformation:")
        transformedChunks.forEachIndexed { index, chunk -> log.info("Chunk nr $index: \n" + chunk.bytes.decodeToString()) }

        log.info("inversed transformation")
        reversedChunks.forEachIndexed { index, chunk -> log.info("Chunk nr $index: \n" + chunk.bytes.decodeToString()) }

        log.info("Validating equality of input and output...")
        chunks.forEachIndexed { index, dataChunk ->
            assert(reversedChunks[index].bytes.contentEquals(dataChunk.bytes))
        }
        log.info("Validation succeeded.")


    }

    @ExperimentalStdlibApi
    @Test
    @Order(5)
    fun transformsChunkLargeFileSync() {
        val chunks = DataChunk.readChunksFromFile("data/$fileToEncode", byteArraySize, log)
        val transformedChunks = mutableListOf<DataChunk>()
        val reversedChunks = mutableListOf<DataChunk>()
        val bwt = BurrowsWheelerTransformation()

        log.info("Performing burrows wheeler transformation on all chunks, adding 2 Byte...")
        chunks.forEach {
            transformedChunks.add(bwt.transformDataChunk(it))
        }
        log.info("Finished burrows wheeler transformation.")
        log.info("Performing inverse burrows wheeler transformation on all chunks...")

        transformedChunks.forEachIndexed { index, it ->
            reversedChunks.add(bwt.invertTransformDataChunk(it))
            if (index % 1000 == 0) {
                log.info("reversing chunk nr $index of ${transformedChunks.size}...")
            }
        }

        log.info("Finished inverse transformation on all ${chunks.size} chunks.")
        log.info("Validating equality of input and output...")

        chunks.forEachIndexed { index, dataChunk ->
            assert(reversedChunks[index].bytes.contentEquals(dataChunk.bytes))
        }
        log.info("Validation succeeded.")
    }

    @ExperimentalStdlibApi
    @Test
    @Order(6)
    fun transformChunksLargeFileAsync() {
        val chunks = DataChunk.readChunksFromFile("data/$fileToEncode", byteArraySize, log)
        val transformedChunks = mutableListOf<DataChunk>()
        val reversedChunks: List<DataChunk>
        val bwt = BurrowsWheelerTransformation()

        log.info("Performing burrows wheeler transformation on all chunks, adding 2 Byte...")
        chunks.forEach {
            transformedChunks.add(bwt.transformDataChunk(it))
        }
        log.info("Finished burrows wheeler transformation.")

        if (DEBUG) {
            log.debug("Writing transformed chunks to $encodeFolder/${fileToEncode}_transformed")
            transformedChunks.stream()
                .forEach { it.appendCurrentChunkToFile("$encodeFolder/${fileToEncode}_transformed") }
        }

        reversedChunks = bwt.invertTransformationParallel(transformedChunks)

        log.info("Finished inverse transformation on all ${chunks.size} chunks.")
        log.info("Writing reversed chunks to $decodeFolder/$fileToEncode")

        reversedChunks.stream().forEach { it.appendCurrentChunkToFile("$decodeFolder/$fileToEncode") }

        log.info("Validating equality of input and output...")

        chunks.forEachIndexed { index, dataChunk ->
            assert(reversedChunks[index].bytes.contentEquals(dataChunk.bytes))
        }
        log.info("Validation succeeded.")
    }
}