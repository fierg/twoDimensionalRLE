package edu.ba.twoDimensionalRLE.tranformation

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.model.DataChunk
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.nio.charset.Charset

@ExperimentalUnsignedTypes
class BurrowsWheelerTransformation {

    private var log = Log.kotlinInstance()
    private val DEBUG = true

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    companion object {
        const val STX = "\u0002"
        const val ETX = "\u0003"
    }

    fun transformDataChunk(input: DataChunk): DataChunk {
        return DataChunk(transform(input.bytes.toString(Charset.defaultCharset())).toByteArray())
    }

    fun invertTransformDataChunk(input: DataChunk): DataChunk {
        return DataChunk(invertTransform(input.bytes.toString(Charset.defaultCharset())).toByteArray())
    }

    fun transform(input: String): String {
        if (input.contains(STX) || input.contains(ETX)) {
            throw RuntimeException("String can't contain STX or ETX")
        }
        val ss = STX + input + ETX
        val table = Array(ss.length) { ss.substring(it) + ss.substring(0, it) }
        table.sort()
        return String(table.map { it[it.lastIndex] }.toCharArray())
    }

    fun invertTransform(input: String): String {
        val length = input.length
        val table = Array(length) { "" }
        repeat(length) {
            for (i in 0 until length) {
                table[i] = input[i].toString() + table[i]
            }
            table.sort()
        }
        for (row in table) {
            if (row.endsWith(ETX)) {
                return row.substring(1, length - 1)
            }
        }
        return ""
    }

    fun invertTransformationParallel(transformedChunks:List<DataChunk>): List<DataChunk> {
        val reversedChunksDeferred = mutableListOf<Deferred<DataChunk>>()

        log.info("Performing inverse burrows wheeler transformation on all chunks in parallel...")
        transformedChunks.forEachIndexed { index, it ->
            reversedChunksDeferred.add(GlobalScope.async {
                if (index % 1000 == 0) {
                    log.info("reversing chunk nr $index of ${transformedChunks.size}...")
                }
                return@async invertTransformDataChunk(it)
            })

        }
        log.info("Started all coroutines...")
        log.info("Awaiting termination of all coroutines...")
        return runBlocking {
            return@runBlocking reversedChunksDeferred.map { it.await() }
        }

    }

    fun makePrintable(input: String): String {
        return input.replace(STX, "^").replace(ETX, "|")
    }


    fun performBurrowsWheelerTransformationOnAllChunks(
        chunks: MutableList<DataChunk>,
        outputFile: String
    ): MutableList<DataChunk> {
        val transformedChunks = mutableListOf<DataChunk>()
        log.info("Performing burrows wheeler transformation on all chunks, adding 2 Byte...")
        chunks.forEach {
            transformedChunks.add(transformDataChunk(it))
        }
        log.info("Finished burrows wheeler transformation.")

        if (DEBUG) {
            transformedChunks.stream().forEach { it.appendCurrentChunkToFile(outputFile + "_transformed") }
        }
        return transformedChunks
    }
}