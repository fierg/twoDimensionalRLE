package edu.ba.twoDimensionalRLE.tranformation

import com.google.common.primitives.SignedBytes
import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.extensions.shift
import edu.ba.twoDimensionalRLE.model.DataChunk
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.lang.IllegalArgumentException

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class BurrowsWheelerTransformationModified {

    private var log = Log.kotlinInstance()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    /*

    This Implementation is partially aligned with the modified version by Burrows and Wheeler:

    see http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.121.6177&rep=rep1&type=pdf

    The Transformation could also be improved.
    Instead of creating the Matrix M with all cyclic rotations, this can also be done in linear time and space by building a suffix tree,
    which then can be walked in lexicographical order to recover the sorted suffixes.
    See McCreight’s suffix tree construction algorithm:

    E.M. McCreight. A space economical suffix tree construction algorithm. Journal of the ACM, Vol. 32, No. 2, April 1976, pp. 262–272.

     */


    fun transform(input: String): Pair<String, Int> {
        val table = Array(input.length) { input.substring(it) + input.substring(0, it) }
        table.sort()
        val index = table.indexOfFirst { it == input }
        return Pair(String(table.map { it[it.lastIndex] }.toCharArray()), index)
    }


    fun transformByteArray(input: ByteArray): Pair<ByteArray, Int> {
        val table = Array(input.size) { input.shift(it)}
        
        table.sortWith(SignedBytes.lexicographicalComparator())
        val index = table.indexOfFirst { it.contentEquals(input) }

        return Pair(table.map { it[it.lastIndex] }.toByteArray(), index)
    }


    fun inverseTransform(L: String, index: Int): String {

        // corresponding to D1. [find first characters of rotations]
        val F = L.toCharArray().sortedArray()

        // corresponding to D2. [build list of predecessor characters]
        val P = IntArray(L.length)
        val C = mutableMapOf<Byte, Int>()

        for (i in 0 .. L.length) {
            P[i] = C.getOrDefault(L[i].toByte(), 0)
            C[L[i].toByte()] = C.getOrDefault(L[i].toByte(), 0) + 1
        }

        var sum = 0

        for (i in 0 until 256) {
            if (C.containsKey(i.toByte())) {
                sum += C[i.toByte()]!!
                C[i.toByte()] = sum - C[i.toByte()]!!
            }
        }

        val S = CharArray(L.length)

        // D3. [form output S]
        var i = index
        for (j in L.length -1 downTo 0){
            S[j] = L[i]
            i = P[i] + C.getOrElse(L[i].toByte()) {throw IllegalArgumentException("Unexpected index!")}
        }

        return S.concatToString()
    }

    fun inverseTransformByteArray(L: ByteArray, index: Int): ByteArray {

        // corresponding to D1. [find first characters of rotations]
        val F = L.sortedArray()

        // corresponding to D2. [build list of predecessor characters]
        val P = IntArray(L.size)
        val C = mutableMapOf<Byte, Int>()

        for (i in 0 .. L.size) {
            P[i] = C.getOrDefault(L[i], 0)
            C[L[i]] = C.getOrDefault(L[i], 0) + 1
        }

        var sum = 0

        for (i in 0 until 256) {
            if (C.containsKey(i.toByte())) {
                sum += C[i.toByte()]!!
                C[i.toByte()] = sum - C[i.toByte()]!!
            }
        }

        val S = ByteArray(L.size)

        // D3. [form output S]
        var i = index
        for (j in L.size-1 downTo 0){
            S[j] = L[i]
            i = P[i] + C.getOrElse(L[i]) {throw IllegalArgumentException("Unexpected index!")}
        }

        return S
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

    private fun invertTransformDataChunk(chunk: DataChunk): DataChunk {
        val result = inverseTransformByteArray(chunk.bytes, chunk.bwtIndex)
        return DataChunk(result)
    }


    fun performModifiedBurrowsWheelerTransformationOnAllChunks(chunks: MutableList<DataChunk>): MutableList<DataChunk> {
        val transformedChunks = mutableListOf<DataChunk>()
        log.info("Performing burrows wheeler transformation on all chunks, adding bwt index to chunk...")
        chunks.forEach {
            transformedChunks.add(transformDataChunk(it))
        }
        log.info("Finished burrows wheeler transformation.")

        return transformedChunks
    }

    private fun transformDataChunk(chunk: DataChunk): DataChunk {
        val result = transformByteArray(chunk.bytes)
        val newChunk = DataChunk(result.first)
        newChunk.bwtIndex = result.second
        return newChunk
    }
}