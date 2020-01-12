package edu.ba.twoDimensionalRLE.tranformation.bijective

import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import kotlin.experimental.and


// The Burrows-Wheeler Transform is a reversible transform based on
// permutation of the data in the original message to reduce the entropy.

// The initial text can be found here:
// Burrows M and Wheeler D, [A block sorting lossless data compression algorithm]
// Technical Report 124, Digital Equipment Corporation, 1994

// See also Peter Fenwick, [Block sorting text compression - final report]
// Technical Report 130, 1996

// This implementation replaces the 'slow' sorting of permutation strings
// with the construction of a suffix array (faster but more complex).
// The suffix array contains the indexes of the sorted suffixes.
//
// E.G.    0123456789A
// Source: mississippi\0
// Suffixes:    rank  sorted
// mississippi\0  0  -> 4             i\0
//  ississippi\0  1  -> 3          ippi\0
//   ssissippi\0  2  -> 10      issippi\0
//    sissippi\0  3  -> 8    ississippi\0
//     issippi\0  4  -> 2   mississippi\0
//      ssippi\0  5  -> 9            pi\0
//       sippi\0  6  -> 7           ppi\0
//        ippi\0  7  -> 1         sippi\0
//         ppi\0  8  -> 6      sissippi\0
//          pi\0  9  -> 5        ssippi\0
//           i\0  10 -> 0     ssissippi\0
// Suffix array SA : 10 7 4 1 0 9 8 6 3 5 2
// BWT[i] = input[SA[i]-1] => BWT(input) = ipssmpissii (+ primary index 5)
// The suffix array and permutation vector are equal when the input is 0 terminated
// The insertion of a guard is done internally and is entirely transparent.
//
// This implementation extends the canonical algorithm to use up to MAX_CHUNKS primary
// indexes (based on input block size). Each primary index corresponds to a data chunk.
// Chunks may be inverted concurrently.

@ExperimentalUnsignedTypes
class BurrowsWheelerTransformation : ByteTransform {
    private var buffer1: IntArray
    private var buffer2: ByteArray
    private var buffer3: ShortArray
    private var buckets: IntArray
    private var freqs: IntArray
    private val primaryIndexes: IntArray
    private var saAlgo: DivSufSort? = null
    private val pool: ExecutorService?
    private val jobs: Int

    // Static allocation of memory
    constructor() {
        buffer1 = IntArray(0)
        buffer2 = ByteArray(0)
        buffer3 = ShortArray(0)
        buckets = IntArray(256)
        freqs = IntArray(256)
        primaryIndexes = IntArray(8)
        pool = null
        jobs = 1
    }

    // Number of jobs provided in the context
    constructor(ctx: Map<String?, Any?>) {
        val tasks = ctx["jobs"] as Int
        require(tasks > 0) { "The number of jobs must be in positive" }
        val threadPool = ctx["pool"] as ExecutorService?
        require(!(tasks > 1 && threadPool == null)) { "The thread pool cannot be null when the number of jobs is $tasks" }
        buffer1 = IntArray(0)
        buffer2 = ByteArray(0)
        buffer3 = ShortArray(0)
        buckets = IntArray(256)
        freqs = IntArray(256)
        primaryIndexes = IntArray(8)
        pool = if (tasks == 1) null else threadPool
        jobs = tasks
    }

    fun getPrimaryIndex(n: Int): Int {
        return primaryIndexes[n]
    }

    // Not thread safe
    fun setPrimaryIndex(n: Int, primaryIndex: Int): Boolean {
        if (primaryIndex < 0 || n < 0 || n >= primaryIndexes.size) return false
        primaryIndexes[n] = primaryIndex
        return true
    }

    // Not thread safe
    override fun forward(src: SliceByteArray, dst: SliceByteArray): Boolean {
        if (src.length == 0) return true
        if (src.array == dst.array) return false
        val count = src.length
        // Not a recoverable error: instead of silently fail the transform,
// issue a fatal error.
        require(count <= maxBlockSize()) { "The max BWT block size is " + maxBlockSize() + ", got " + count }
        if (dst.index + count > dst.array!!.size) return false
        val input = src.array
        val output = dst.array
        val srcIdx = src.index
        val dstIdx = dst.index
        if (count < 2) {
            if (count == 1) output!![dst.index++] = input!![src.index++]
            return true
        }
        // Lazy dynamic memory allocation
        if (saAlgo == null) saAlgo = DivSufSort()
        if (buffer1.size < count) buffer1 = IntArray(count)
        val sa = buffer1
        saAlgo!!.computeSuffixArray(input!!, sa, srcIdx, count)
        val srcIdx2 = srcIdx - 1
        val dstIdx2 = dstIdx + 1
        val chunks = getBWTChunks(count)
        var res = true
        if (chunks == 1) {
            output!![dstIdx] = input[srcIdx2 + count]
            var n = 0
            while (n < count) {
                if (sa[n] == 0) break
                output[dstIdx2 + n] = input[srcIdx2 + sa[n]]
                n++
            }
            n++
            res = res and setPrimaryIndex(0, n)
            while (n < count) {
                output[dstIdx + n] = input[srcIdx2 + sa[n]]
                n++
            }
        } else {
            val st = count / chunks
            val step = if (chunks * st == count) st else st + 1
            output!![dstIdx] = input[srcIdx2 + count]
            var idx = 0
            for (i in 0 until count) {
                if (sa[i] % step != 0) continue
                res = res and setPrimaryIndex(sa[i] / step, i + 1)
                idx++
                if (idx == chunks) break
            }
            val pIdx0 = getPrimaryIndex(0)
            for (i in 0 until pIdx0 - 1) output[dstIdx2 + i] = input[srcIdx2 + sa[i]]
            for (i in pIdx0 until count) output[dstIdx + i] = input[srcIdx2 + sa[i]]
        }
        src.index += count
        dst.index += count
        return res
    }

    // Not thread safe
    override fun inverse(src: SliceByteArray, dst: SliceByteArray): Boolean {
        if (src.length == 0) return true
        if (src.array == dst.array) return false
        val count = src.length
        // Not a recoverable error: instead of silently fail the transform,
// issue a fatal error.
        require(count <= maxBlockSize()) { "The max BWT block size is " + maxBlockSize() + ", got " + count }
        if (dst.index + count > dst.array!!.size) return false
        if (count < 2) {
            if (count == 1) dst.array!![dst.index++] = src.array!![src.index++]
            return true
        }
        // Find the fastest way to implement inverse based on block size
        return if (count < 4 * 1024 * 1024) inverseSmallBlock(src, dst, count) else inverseBigBlock(src, dst, count)
    }

    // When count < 4M, mergeTPSI algo. Always in one chunk
    private fun inverseSmallBlock(
        src: SliceByteArray,
        dst: SliceByteArray,
        count: Int
    ): Boolean { // Lazy dynamic memory allocation
        if (buffer1.size < count) buffer1 = IntArray(count)
        // Aliasing
        val input = src.array
        val output = dst.array
        val srcIdx = src.index
        val dstIdx = dst.index
        val buckets_ = buckets
        val data = buffer1
        // Build array of packed index + value (assumes block size < 2^24)
        val pIdx = getPrimaryIndex(0)
        if (pIdx < 0 || pIdx > count) return false
        Global.computeHistogramOrder0(input!!, srcIdx, srcIdx + count, buckets, false)
        run {
            var i = 0
            var sum = 0
            while (i < 256) {
                val tmp = buckets_[i]
                buckets_[i] = sum
                sum += tmp
                i++
            }
        }
        for (i in 0 until pIdx) {
            val `val`: Int = (input[srcIdx + i] and 0xFF.toByte()).toInt()
            data[buckets_[`val`]] = i - 1 shl 8 or `val`
            buckets_[`val`]++
        }
        for (i in pIdx until count) {
            val `val`: Int = (input[srcIdx + i] and 0xFF.toByte()).toInt()
            data[buckets_[`val`]] = i shl 8 or `val`
            buckets_[`val`]++
        }
        var i = 0
        var t = pIdx - 1
        while (i < count) {
            val ptr = data[t]
            output!![dstIdx + i] = ptr.toByte()
            t = ptr ushr 8
            i++
        }
        src.index += count
        dst.index += count
        return true
    }

    // When count >= 1<<24, biPSIv2 algo
// Possibly multiple chunks
    private fun inverseBigBlock(
        src: SliceByteArray,
        dst: SliceByteArray,
        count: Int
    ): Boolean { // Lazy dynamic memory allocations
        if (buffer1.size < count + 1) buffer1 = IntArray(count + 1)
        if (buckets.size < 65536) buckets = IntArray(65536)
        if (buffer3.size < MASK_FASTBITS + 1) buffer3 =
            ShortArray(MASK_FASTBITS + 1)
        // Aliasing
        val input = src.array
        val output = dst.array
        val srcIdx = src.index
        val dstIdx = dst.index
        val srcIdx2 = src.index - 1
        val pIdx = getPrimaryIndex(0)
        if (pIdx < 0 || pIdx > count) return false
        Global.computeHistogramOrder0(input!!, srcIdx, srcIdx + count, freqs, false)
        val buckets_ = buckets
        val freqs_ = freqs
        run {
            var sum = 1
            var c = 0
            while (c < 256) {
                val f = sum
                sum += freqs_[c]
                freqs_[c] = f
                if (f != sum) {
                    val c256 = c shl 8
                    val hi = if (sum < pIdx) sum else pIdx
                    for (i in f until hi) buckets_[c256 or (input[srcIdx + i]).toInt()]++
                    val lo = if (f - 1 > pIdx) f - 1 else pIdx
                    for (i in lo until sum - 1) buckets_[c256 or (input[srcIdx + i]).toInt()]++
                }
                c++
            }
        }
        val lastc: Int = input[srcIdx].toInt()
        val fastBits = buffer3
        var shift = 0
        while (count ushr shift > MASK_FASTBITS) shift++
        run {
            var v = 0
            var sum = 1
            var c = 0
            while (c < 256) {
                if (c == lastc) sum++
                for (d in 0..255) {
                    val s = sum
                    sum += buckets_[d shl 8 or c]
                    buckets_[d shl 8 or c] = s
                    if (s != sum) {
                        while (v <= sum - 1 shr shift) {
                            fastBits[v] = (c shl 8 or d).toShort()
                            v++
                        }
                    }
                }
                c++
            }
        }
        val data = buffer1
        for (i in 0 until pIdx) {
            val c: Int = input[srcIdx + i].toInt()
            val p = freqs_[c]
            freqs_[c]++
            if (p < pIdx) {
                val idx = c shl 8 or (input[srcIdx + p].toInt())
                data[buckets_[idx]] = i
                buckets_[idx]++
            } else if (p > pIdx) {
                val idx = c shl 8 or (input[srcIdx2 + p].toInt())
                data[buckets_[idx]] = i
                buckets_[idx]++
            }
        }
        for (i in pIdx until count) {
            val c: Int = input[srcIdx + i].toInt()
            val p = freqs_[c]
            freqs_[c]++
            if (p < pIdx) {
                val idx = c shl 8 or (input[srcIdx + p].toInt())
                data[buckets_[idx]] = i + 1
                buckets_[idx]++
            } else if (p > pIdx) {
                val idx = c shl 8 or (input[srcIdx2 + p].toInt())
                data[buckets_[idx]] = i + 1
                buckets_[idx]++
            }
        }
        for (c in 0..255) {
            val c256 = c shl 8
            for (d in 0 until c) {
                val tmp = buckets_[d shl 8 or c]
                buckets_[d shl 8 or c] = buckets_[c256 or d]
                buckets_[c256 or d] = tmp
            }
        }
        val chunks = getBWTChunks(count)
        // Build inverse
        if (chunks == 1) { // Shortcut for 1 chunk scenario
            var i = 1
            var p = pIdx
            while (i < count) {
                var c: Int = fastBits[p ushr shift].toInt()
                while (buckets_[c] <= p) c++
                output!![dstIdx + i - 1] = (c ushr 8).toByte()
                output[dstIdx + i] = c.toByte()
                p = data[p]
                i += 2
            }
        } else { // Several chunks may be decoded concurrently (depending on the availability
// of jobs in the pool).
            val st = count / chunks
            val ckSize = if (chunks * st == count) st else st + 1
            val nbTasks = if (jobs <= chunks) jobs else chunks
            val tasks: MutableList<Callable<Int>> = ArrayList(nbTasks)
            val jobsPerTask: IntArray = Global.computeJobsPerTask(IntArray(nbTasks), chunks, nbTasks)
            // Create one task per job
            var j = 0
            var c = 0
            while (j < nbTasks) {
                // Each task decodes jobsPerTask[j] chunks
                val start = dstIdx + c * ckSize
                tasks.add(InverseBigChunkTask(output!!, start, count, ckSize, c, c + jobsPerTask[j]))
                c += jobsPerTask[j]
                j++
            }
            try {
                if (jobs == 1) {
                    tasks[0].call()
                } else { // Wait for completion of all concurrent tasks
                    for (result in pool!!.invokeAll(tasks)) result.get()
                }
            } catch (e: Exception) {
                return false
            }
        }
        output!![dstIdx + count - 1] = lastc.toByte()
        src.index += count
        dst.index += count
        return true
    }

    // Process one or several chunk(s)
    internal inner class InverseBigChunkTask(
        private val output: ByteArray, // initial offset
        private val dstIdx: Int, // max number of bytes to process
        private val total: Int,
        // chunk size, must be adjusted to not go over total
        private val ckSize: Int, // index first chunk
        private val firstChunk: Int, // index last chunk
        private val lastChunk: Int
    ) : Callable<Int> {

        override fun call(): Int {
            val data = buffer1
            val buckets = buckets
            val fastBits = buffer3
            var start = dstIdx
            var shift = 0
            while (total ushr shift > MASK_FASTBITS) shift++
            // Process each chunk sequentially
            for (c in firstChunk until lastChunk) {
                val end = if (start + ckSize > total - 1) total - 1 else start + ckSize
                var p = getPrimaryIndex(c)
                var i = start + 1
                while (i <= end) {
                    var s: Int = fastBits[p shr shift].toInt()
                    while (buckets[s] <= p) s++
                    output[i - 1] = (s ushr 8).toByte()
                    output[i] = s.toByte()
                    p = data[p]
                    i += 2
                }
                start = end
            }
            return 0
        }

    }

    companion object {
        private const val MAX_BLOCK_SIZE = 1024 * 1024 * 1024 // 1 GB
        private const val MAX_CHUNKS = 8
        private const val NB_FASTBITS = 17
        private const val MASK_FASTBITS = 1 shl NB_FASTBITS
        fun maxBlockSize(): Int {
            return MAX_BLOCK_SIZE
        }

        fun getBWTChunks(size: Int): Int {
            return if (size < 4 * 1024 * 1024) 1 else Math.min(size + (1 shl 21) shr 22, MAX_CHUNKS)
        }
    }
}