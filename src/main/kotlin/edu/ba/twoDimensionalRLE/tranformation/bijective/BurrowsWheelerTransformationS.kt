package edu.ba.twoDimensionalRLE.tranformation.bijective

import kotlin.experimental.and


// Port to Kotlin by Sven Fiergolla, original port to (proper) Java of the DivSufSort algorithm by Yuta Mori.

class BurrowsWheelerTransformationS : ByteTransform {
    private var buffer1: IntArray
    private var buffer2: IntArray
    private val buckets: IntArray
    private var saAlgo: DivSufSort? = null

    constructor() {
        buffer1 = IntArray(0)
        buffer2 = IntArray(0)
        buckets = IntArray(256)
    }

    constructor(ctx: Map<String?, Any?>?) {
        buffer1 = IntArray(0)
        buffer2 = IntArray(0)
        buckets = IntArray(256)
    }

    // Not thread safe
    override fun forward(src: SliceByteArray, dst: SliceByteArray): Boolean {
        if (src.length === 0) return true
        if (src.array === dst.array) return false
        val count: Int = src.length
        // Not a recoverable error: instead of silently fail the transform,
// issue a fatal error.
        require(count <= maxBlockSize()) { "The max BWTS block size is " + maxBlockSize() + ", got " + count }
        if (dst.index + count > dst.array!!.size) return false
        val input: ByteArray = src.array!!
        val output: ByteArray = dst.array!!
        val srcIdx: Int = src.index
        val dstIdx: Int = dst.index
        if (count < 2) {
            if (count == 1) output[dst.index++] = input[src.index++]
            return true
        }
        if (saAlgo == null) saAlgo = DivSufSort()
        // Lazy dynamic memory allocations
        if (buffer1.size < count) buffer1 = IntArray(count)
        if (buffer2.size < count) buffer2 = IntArray(count)
        // Aliasing
        val sa = buffer1
        val isa = buffer2
        saAlgo!!.computeSuffixArray(input, sa, srcIdx, count)
        for (i in 0 until count) isa[sa[i]] = i
        var min = isa[0]
        var idxMin = 0
        run {
            var i = 1
            while (i < count && min > 0) {
                if (isa[i] >= min) {
                    i++
                    continue
                }
                var refRank =
                    moveLyndonWordHead(sa, isa, input, count, srcIdx, idxMin, i - idxMin, min)
                for (j in i - 1 downTo idxMin + 1) { // iterate through the new lyndon word from end to start
                    var testRank = isa[j]
                    val startRank = testRank
                    while (testRank < count - 1) {
                        val nextRankStart = sa[testRank + 1]
                        if (j > nextRankStart || input[srcIdx + j] != input[srcIdx + nextRankStart]
                            || refRank < isa[nextRankStart + 1]
                        ) break
                        sa[testRank] = nextRankStart
                        isa[nextRankStart] = testRank
                        testRank++
                    }
                    sa[testRank] = j
                    isa[j] = testRank
                    refRank = testRank
                    if (startRank == testRank) break
                }
                min = isa[i]
                idxMin = i
                i++
            }
        }
        min = count
        val srcIdx2 = srcIdx - 1
        for (i in 0 until count) {
            if (isa[i] >= min) {
                output[dstIdx + isa[i]] = input[srcIdx2 + i]
                continue
            }
            if (min < count) output[dstIdx + min] = input[srcIdx2 + i]
            min = isa[i]
        }
        output[dstIdx] = input[srcIdx2 + count]
        src.index += count
        dst.index += count
        return true
    }

    // Not thread safe
    override fun inverse(src: SliceByteArray, dst: SliceByteArray): Boolean {
        if (src.length === 0) return true
        if (src.array === dst.array) return false
        val count: Int = src.length
        // Not a recoverable error: instead of silently fail the transform,
// issue a fatal error.
        require(count <= maxBlockSize()) { "The max BWTS block size is " + maxBlockSize() + ", got " + count }
        if (dst.index + count > dst.array!!.size) return false
        if (count < 2) {
            if (count == 1) dst.array!![dst.index++] = src.array!![src.index++]
            return true
        }
        val input: ByteArray = src.array!!
        val output: ByteArray = dst.array!!
        val srcIdx: Int = src.index
        val dstIdx: Int = dst.index
        // Lazy dynamic memory allocation
        if (buffer1.size < count) buffer1 = IntArray(count)
        // Aliasing
        val buckets_ = buckets
        val lf = buffer1
        // Initialize histogram
        for (i in 0..255) buckets_[i] = 0
        for (i in 0 until count) buckets_[(input[srcIdx + i] and 0xFF.toByte()).toInt()]++
        // Histogram
        run {
            var i = 0
            var sum = 0
            while (i < 256) {
                sum += buckets_[i]
                buckets_[i] = sum - buckets_[i]
                i++
            }
        }
        for (i in 0 until count) lf[i] = buckets_[(input[srcIdx + i] and 0xFF.toByte()).toInt()]++
        // Build inverse
        var i = 0
        var j = dstIdx + count - 1
        while (j >= dstIdx) {
            if (lf[i] < 0) {
                i++
                continue
            }
            var p = i
            do {
                output[j] = input[srcIdx + p]
                j--
                val t = lf[p]
                lf[p] = -1
                p = t
            } while (lf[p] >= 0)
            i++
        }
        src.index += count
        dst.index += count
        return true
    }

    companion object {
        private const val MAX_BLOCK_SIZE = 1024 * 1024 * 1024 // 1 GB
        private fun moveLyndonWordHead(
            sa: IntArray,
            isa: IntArray,
            data: ByteArray,
            count: Int,
            srcIdx: Int,
            start: Int,
            size: Int,
            rank: Int
        ): Int {
            var rank = rank
            val end = start + size
            val startIdx = srcIdx + start
            while (rank + 1 < count) {
                val nextStart0 = sa[rank + 1]
                if (nextStart0 <= end) break
                var nextStart = nextStart0
                var k = 0
                while (k < size && nextStart < count && data[startIdx + k] == data[srcIdx + nextStart]) {
                    k++
                    nextStart++
                }
                if (k == size && rank < isa[nextStart]) break
                if (k < size && nextStart < count && data[startIdx + k] and 0xFF.toByte() < data[srcIdx + nextStart] and 0xFF.toByte()) break
                sa[rank] = nextStart0
                isa[nextStart0] = rank
                rank++
            }
            sa[rank] = start
            isa[start] = rank
            return rank
        }

        fun maxBlockSize(): Int {
            return MAX_BLOCK_SIZE
        }
    }
}