package edu.ba.twoDimensionalRLE.tranformation.bijective

import edu.ba.twoDimensionalRLE.extensions.shl
import kotlin.experimental.inv


// Port to (proper) Java of the DivSufSort algorithm by Yuta Mori.
// DivSufSort is a fast two-stage suffix sorting algorithm.
// The original C code is here: https://code.google.com/p/libdivsufsort/
// See also https://code.google.com/p/libdivsufsort/source/browse/wiki/SACA_Benchmarks.wiki
// for comparison of different suffix array construction algorithms.
// It is used to implement the forward stage of the BWT in linear time.

// Port to (proper) Java of the DivSufSort algorithm by Yuta Mori.
// DivSufSort is a fast two-stage suffix sorting algorithm.
// The original C code is here: https://code.google.com/p/libdivsufsort/
// See also https://code.google.com/p/libdivsufsort/source/browse/wiki/SACA_Benchmarks.wiki
// for comparison of different suffix array construction algorithms.
// It is used to implement the forward stage of the BWT in linear time.
@ExperimentalUnsignedTypes
class DivSufSort {
    private var sa: IntArray
    private var buffer: ShortArray
    private val bucketA: IntArray = IntArray(256)
    private val bucketB: IntArray = IntArray(65536)

    private val ssStack: Stack
    private val trStack: Stack
    private val mergeStack: Stack
    private fun reset() {
        ssStack.index = 0
        trStack.index = 0
        mergeStack.index = 0
        for (i in bucketA.indices.reversed()) bucketA[i] = 0
        for (i in bucketB.indices.reversed()) bucketB[i] = 0
    }

    // Not thread safe
    fun computeSuffixArray(
        input: ByteArray,
        sa: IntArray,
        start: Int,
        length: Int
    ) { // Lazy dynamic memory allocation
        if (buffer.size < length) buffer = ShortArray(length)
        for (i in 0 until length) buffer[i] = input[start + i].toShort()
        this.sa = sa
        reset()
        val m = sortTypeBstar(bucketA, bucketB, length)
        constructSuffixArray(bucketA, bucketB, length, m)
    }

    private fun constructSuffixArray(bucketA: IntArray, bucketB: IntArray, n: Int, m: Int) {
        if (m > 0) {
            for (c1 in 254 downTo 0) {
                val idx = c1 shl 8
                val i = bucketB[idx + c1 + 1]
                var k = 0
                var c2 = -1
                // Scan the suffix array from right to left.
                for (j in bucketA[c1 + 1] - 1 downTo i) {
                    var s = sa[j]
                    sa[j] = s.inv()
                    if (s <= 0) continue
                    s--
                    val c0 = buffer[s].toInt()
                    if (s > 0 && buffer[s - 1] > c0) s = s.inv()
                    if (c0 != c2) {
                        if (c2 >= 0) bucketB[idx + c2] = k
                        c2 = c0
                        k = bucketB[idx + c2]
                    }
                    sa[k--] = s
                }
            }
        }
        var c2 = buffer[n - 1].toUInt().toInt()
        var k = bucketA[c2]
        sa[k++] = if (buffer[n - 2] < c2) (n - 1).inv() else n - 1
        // Scan the suffix array from left to right.
        for (i in 0 until n) {
            var s = sa[i]
            if (s <= 0) {
                sa[i] = s.inv()
                continue
            }
            s--
            val c0 = buffer[s].toInt()
            if (s == 0 || buffer[s - 1] < c0) s = s.inv()
            if (c0 != c2) {
                bucketA[c2] = k
                c2 = c0
                k = bucketA[c2]
            }
            sa[k++] = s
        }
    }

    // Not thread safe
    fun computeBWT(
        input: ByteArray,
        sa: IntArray,
        start: Int,
        length: Int
    ): Int { // Lazy dynamic memory allocation
        if (buffer.size < length) buffer = ShortArray(length)
        for (i in 0 until length) buffer[i] = input[start + i].toShort()
        this.sa = sa
        reset()
        val m = sortTypeBstar(bucketA, bucketB, length)
        return constructBWT(bucketA, bucketB, length, m)
    }

    private fun constructBWT(bucketA: IntArray, bucketB: IntArray, n: Int, m: Int): Int {
        var pIdx = -1
        if (m > 0) {
            for (c1 in 254 downTo 0) {
                val idx = c1 shl 8
                val i = bucketB[idx + c1 + 1]
                var k = 0
                var c2 = -1
                // Scan the suffix array from right to left.
                for (j in bucketA[c1 + 1] - 1 downTo i) {
                    var s = sa[j]
                    if (s <= 0) {
                        if (s != 0) sa[j] = s.inv()
                        continue
                    }
                    s--
                    val c0 = buffer[s].toInt()
                    sa[j] = c0.inv()
                    if (s > 0 && buffer[s - 1] > c0) s = s.inv()
                    if (c0 != c2) {
                        if (c2 >= 0) bucketB[idx + c2] = k
                        c2 = c0
                        k = bucketB[idx + c2]
                    }
                    sa[k--] = s
                }
            }
        }
        var c2 = buffer[n - 1].toInt()
        var k = bucketA[c2]
        sa[k++] = if (buffer[n - 2] < c2) buffer[n - 2].inv().toInt() else n - 1
        // Scan the suffix array from left to right.
        for (i in 0 until n) {
            var s = sa[i]
            if (s <= 0) {
                if (s != 0) sa[i] = s.inv() else pIdx = i
                continue
            }
            s--
            val c0 = buffer[s].toInt()
            sa[i] = c0
            if (s > 0 && buffer[s - 1] < c0) s = buffer[s - 1].inv().toInt()
            if (c0 != c2) {
                bucketA[c2] = k
                c2 = c0
                k = bucketA[c2]
            }
            sa[k++] = s
        }
        return pIdx
    }

    private fun sortTypeBstar(bucketA: IntArray, bucketB: IntArray, n: Int): Int {
        var m = n
        var c0 = buffer[n - 1].toUInt().toInt()
        val arr = sa
        // Count the number of occurrences of the first one or two characters of each
// type A, B and B* suffix. Moreover, store the beginning position of all
// type B* suffixes into the array SA.
        run {
            var i = n - 1
            while (i >= 0) {
                var c1: Int
                do {
                    c1 = c0
                    bucketA[c1]++
                    i--
                } while (i >= 0 && this.buffer[i].also { c0 = it.toUByte().toInt() } >= c1)
                if (i < 0) break
                bucketB[(c0 shl 8) + c1]++
                m--
                arr[m] = i
                i--
                c1 = c0
                while (i >= 0 && this.buffer[i].also { c0 = it.toInt() } <= c1) {
                    bucketB[(c1 shl 8) + c0]++
                    c1 = c0
                    i--
                }
            }
        }
        m = n - m
        c0 = 0
        // A type B* suffix is lexicographically smaller than a type B suffix that
// begins with the same first two characters.
// Calculate the index of start/end point of each bucket.
        var i = 0
        var j = 0
        while (c0 < 256) {
            val t = i + bucketA[c0]
            bucketA[c0] = i + j // start point
            val idx = c0 shl 8
            i = t + bucketB[idx + c0]
            for (c1 in c0 + 1..255) {
                j += bucketB[idx + c1]
                bucketB[idx + c1] = j // end point
                i += bucketB[(c1 shl 8) + c0]
            }
            c0++
        }
        if (m > 0) { // Sort the type B* suffixes by their first two characters.
            val pab = n - m
            for (i in m - 2 downTo 0) {
                val t = arr[pab + i]
                val idx: Int = (buffer[t] shl 8) + buffer[t + 1]
                bucketB[idx]--
                arr[bucketB[idx]] = i
            }
            val t = arr[pab + m - 1]
            c0 = (buffer[t] shl 8) + buffer[t + 1]
            bucketB[c0]--
            arr[bucketB[c0]] = m - 1
            // Sort the type B* substrings using ssSort.
            val bufSize = n - m - m
            c0 = 254
            run {
                var j = m
                while (j > 0) {
                    val idx = c0 shl 8
                    for (c1 in 255 downTo c0 + 1) {
                        val i = bucketB[idx + c1]
                        if (j > i + 1) this.ssSort(pab, i, j, m, bufSize, 2, n, arr[i] == m - 1)
                        j = i
                    }
                    c0--
                }
            }
            // Compute ranks of type B* substrings.
            run {
                var i = m - 1
                while (i >= 0) {
                    if (arr[i] >= 0) {
                        val j = i
                        do {
                            arr[m + arr[i]] = i
                            i--
                        } while (i >= 0 && arr[i] >= 0)
                        arr[i + 1] = i - j
                        if (i <= 0) break
                    }
                    val j = i
                    do {
                        arr[i] = arr[i].inv()
                        arr[m + arr[i]] = j
                        i--
                    } while (arr[i] < 0)
                    arr[m + arr[i]] = j
                    i--
                }
            }
            // Construct the inverse suffix array of type B* suffixes using trSort.
            trSort(m, 1)
            // Set the sorted order of type B* suffixes.
            c0 = buffer[n - 1].toInt()
            var i = n - 1
            var j = m
            while (i >= 0) {
                i--
                var c1 = c0
                while (i >= 0 && buffer[i].also { c0 = it.toInt() } >= c1) {
                    c1 = c0
                    i--
                }
                if (i >= 0) {
                    val tt = i
                    i--
                    var c1 = c0
                    while (i >= 0 && buffer[i].also { c0 = it.toInt() } <= c1) {
                        c1 = c0
                        i--
                    }
                    j--
                    arr[arr[m + j]] = if (tt == 0 || tt - i > 1) tt else tt.inv()
                }
            }
            // Calculate the index of start/end point of each bucket.
            bucketB[bucketB.size - 1] = n // end
            var k = m - 1
            c0 = 254
            while (c0 >= 0) {
                var i = bucketA[c0 + 1] - 1
                val idx = c0 shl 8
                for (c1 in 255 downTo c0 + 1) {
                    val tt = i - bucketB[(c1 shl 8) + c0]
                    bucketB[(c1 shl 8) + c0] = i // end point
                    i = tt
                    // Move all type B* suffixes to the correct position.
// Typically very small number of copies, no need for arraycopy
                    val j = bucketB[idx + c1]
                    while (j <= k) {
                        arr[i] = arr[k]
                        i--
                        k--
                    }
                }
                bucketB[idx + c0 + 1] = i - bucketB[idx + c0] + 1 //start point
                bucketB[idx + c0] = i // end point
                c0--
            }
        }
        return m
    }

    // Sub String Sort
    private fun ssSort(
        pa: Int, first: Int, last: Int, buf: Int, bufSize: Int,
        depth: Int, n: Int, lastSuffix: Boolean
    ) {
        var first = first
        var buf = buf
        var bufSize = bufSize
        if (lastSuffix == true) first++
        var limit = 0
        var middle = last
        if (bufSize < SS_BLOCKSIZE && bufSize < last - first) {
            limit = ssIsqrt(last - first)
            if (bufSize < limit) {
                if (limit > SS_BLOCKSIZE) limit = SS_BLOCKSIZE
                middle = last - limit
                buf = middle
                bufSize = limit
            } else {
                limit = 0
            }
        }
        var a: Int
        var i = 0
        a = first
        while (middle - a > SS_BLOCKSIZE) {
            ssMultiKeyIntroSort(pa, a, a + SS_BLOCKSIZE, depth)
            var curBufSize = last - (a + SS_BLOCKSIZE)
            val curBuf: Int
            if (curBufSize > bufSize) {
                curBuf = a + SS_BLOCKSIZE
            } else {
                curBufSize = bufSize
                curBuf = buf
            }
            var k = SS_BLOCKSIZE
            var b = a
            var j = i
            while (j and 1 != 0) {
                ssSwapMerge(pa, b - k, b, b + k, curBuf, curBufSize, depth)
                b -= k
                k = k shl 1
                j = j shr 1
            }
            a += SS_BLOCKSIZE
            i++
        }
        ssMultiKeyIntroSort(pa, a, middle, depth)
        var k = SS_BLOCKSIZE
        while (i != 0) {
            if (i and 1 == 0) {
                k = k shl 1
                i = i shr 1
                continue
            }
            ssSwapMerge(pa, a - k, a, middle, buf, bufSize, depth)
            a -= k
            k = k shl 1
            i = i shr 1
        }
        if (limit != 0) {
            ssMultiKeyIntroSort(pa, middle, last, depth)
            ssInplaceMerge(pa, first, middle, last, depth)
        }
        if (lastSuffix == true) {
            i = sa[first - 1]
            val p1 = sa[pa + i]
            val p11 = n - 2
            a = first
            while (a < last && (sa[a] < 0 || this.ssCompare(p1, p11, pa + sa[a], depth) > 0)) {
                sa[a - 1] = sa[a]
                a++
            }
            sa[a - 1] = i
        }
    }

    private fun ssCompare(pa: Int, pb: Int, p2: Int, depth: Int): Int {
        var u1 = depth + pa
        var u2 = depth + sa[p2]
        val u1n = pb + 2
        val u2n = sa[p2 + 1] + 2
        if (u1n - u1 > u2n - u2) {
            while (u2 < u2n && buffer[u1] == buffer[u2]) {
                u1++
                u2++
            }
        } else {
            while (u1 < u1n && buffer[u1] == buffer[u2]) {
                u1++
                u2++
            }
        }
        return if (u1 < u1n) if (u2 < u2n) buffer[u1] - buffer[u2] else 1 else if (u2 < u2n) -1 else 0
    }

    private fun ssCompare(p1: Int, p2: Int, depth: Int): Int {
        var u1 = depth + sa[p1]
        var u2 = depth + sa[p2]
        val u1n = sa[p1 + 1] + 2
        val u2n = sa[p2 + 1] + 2
        if (u1n - u1 > u2n - u2) {
            while (u2 < u2n && buffer[u1] == buffer[u2]) {
                u1++
                u2++
            }
        } else {
            while (u1 < u1n && buffer[u1] == buffer[u2]) {
                u1++
                u2++
            }
        }
        return if (u1 < u1n) if (u2 < u2n) buffer[u1] - buffer[u2] else 1 else if (u2 < u2n) -1 else 0
    }

    private fun ssInplaceMerge(pa: Int, first: Int, middle: Int, last: Int, depth: Int) {
        var middle = middle
        var last = last
        val arr = sa
        while (true) {
            var p: Int
            var x: Int
            if (arr[last - 1] < 0) {
                x = 1
                p = pa + arr[last - 1].inv()
            } else {
                x = 0
                p = pa + arr[last - 1]
            }
            var a = first
            var r = -1
            var len = middle - first
            var half = len shr 1
            while (len > 0) {
                val b = a + half
                val q = ssCompare(pa + if (arr[b] >= 0) arr[b] else arr[b].inv(), p, depth)
                if (q < 0) {
                    a = b + 1
                    half -= len and 1 xor 1
                } else r = q
                len = half
                half = half shr 1
            }
            if (a < middle) {
                if (r == 0) arr[a] = arr[a].inv()
                ssRotate(a, middle, last)
                last -= middle - a
                middle = a
                if (first == middle) break
            }
            last--
            if (x != 0) {
                last--
                while (arr[last] < 0) last--
            }
            if (middle == last) break
        }
    }

    private fun ssRotate(first: Int, middle: Int, last: Int) {
        var first = first
        var last = last
        var l = middle - first
        var r = last - middle
        val arr = sa
        while (l > 0 && r > 0) {
            if (l == r) {
                ssBlockSwap(first, middle, l)
                break
            }
            if (l < r) {
                var a = last - 1
                var b = middle - 1
                var t = arr[a]
                while (true) {
                    arr[a--] = arr[b]
                    arr[b--] = arr[a]
                    if (b < first) {
                        arr[a] = t
                        last = a
                        r -= l + 1
                        if (r <= l) break
                        a--
                        b = middle - 1
                        t = arr[a]
                    }
                }
            } else {
                var a = first
                var b = middle
                var t = arr[a]
                while (true) {
                    arr[a++] = arr[b]
                    arr[b++] = arr[a]
                    if (last <= b) {
                        arr[a] = t
                        first = a + 1
                        l -= r + 1
                        if (l <= r) break
                        a++
                        b = middle
                        t = arr[a]
                    }
                }
            }
        }
    }

    private fun ssBlockSwap(a: Int, b: Int, n: Int) {
        var a = a
        var b = b
        var n = n
        while (n > 0) {
            val t = sa[a]
            sa[a] = sa[b]
            sa[b] = t
            n--
            a++
            b++
        }
    }

    private fun ssSwapMerge(
        pa: Int, first: Int, middle: Int, last: Int, buf: Int,
        bufSize: Int, depth: Int
    ) {
        var first = first
        var middle = middle
        var last = last
        val arr = sa
        var check = 0
        while (true) {
            if (last - middle <= bufSize) {
                if (first < middle && middle < last) ssMergeBackward(pa, first, middle, last, buf, depth)
                if (check and 1 != 0
                    || check and 2 != 0 && this.ssCompare(
                        pa + getIndex(sa[first - 1]),
                        pa + arr[first], depth
                    ) == 0
                ) {
                    arr[first] = arr[first].inv()
                }
                if (check and 4 != 0
                    && this.ssCompare(
                        pa + getIndex(arr[last - 1]),
                        pa + arr[last],
                        depth
                    ) == 0
                ) {
                    arr[last] = arr[last].inv()
                }
                val se = mergeStack.pop() ?: return
                first = se.a
                middle = se.b
                last = se.c
                check = se.d
                continue
            }
            if (middle - first <= bufSize) {
                if (first < middle) ssMergeForward(pa, first, middle, last, buf, depth)
                if (check and 1 != 0
                    || check and 2 != 0 && ssCompare(
                        pa + getIndex(arr[first - 1]),
                        pa + arr[first], depth
                    ) == 0
                ) {
                    arr[first] = arr[first].inv()
                }
                if (check and 4 != 0
                    && ssCompare(
                        pa + getIndex(arr[last - 1]),
                        pa + arr[last],
                        depth
                    ) == 0
                ) {
                    arr[last] = arr[last].inv()
                }
                val se = mergeStack.pop() ?: return
                first = se.a
                middle = se.b
                last = se.c
                check = se.d
                continue
            }
            var len = if (middle - first < last - middle) middle - first else last - middle
            var m = 0
            var half = len shr 1
            while (len > 0) {
                if (ssCompare(
                        pa + getIndex(arr[middle + m + half]),
                        pa + getIndex(arr[middle - m - half - 1]),
                        depth
                    ) < 0
                ) {
                    m += half + 1
                    half -= len and 1 xor 1
                }
                len = half
                half = half shr 1
            }
            if (m > 0) {
                val lm = middle - m
                val rm = middle + m
                ssBlockSwap(lm, middle, m)
                var l = middle
                var r = l
                var next = 0
                if (rm < last) {
                    if (arr[rm] < 0) {
                        arr[rm] = arr[rm].inv()
                        if (first < lm) {
                            l--
                            while (arr[l] < 0) l--
                            next = next or 4
                        }
                        next = next or 1
                    } else if (first < lm) {
                        while (arr[r] < 0) r++
                        next = next or 2
                    }
                }
                if (l - first <= last - r) {
                    mergeStack.push(r, rm, last, next and 3 or (check and 4), 0)
                    middle = lm
                    last = l
                    check = check and 3 or (next and 4)
                } else {
                    if (r == middle && next and 2 != 0) next = next xor 6
                    mergeStack.push(first, lm, l, check and 3 or (next and 4), 0)
                    first = r
                    middle = rm
                    check = next and 3 or (check and 4)
                }
            } else {
                if (this.ssCompare(
                        pa + getIndex(arr[middle - 1]),
                        pa + arr[middle],
                        depth
                    ) == 0
                ) {
                    arr[middle] = arr[middle].inv()
                }
                if (check and 1 != 0
                    || check and 2 != 0 && this.ssCompare(
                        pa + getIndex(sa[first - 1]),
                        pa + arr[first], depth
                    ) == 0
                ) {
                    arr[first] = arr[first].inv()
                }
                if (check and 4 != 0
                    && this.ssCompare(
                        pa + getIndex(arr[last - 1]),
                        pa + arr[last],
                        depth
                    ) == 0
                ) {
                    arr[last] = arr[last].inv()
                }
                val se = mergeStack.pop() ?: return
                first = se.a
                middle = se.b
                last = se.c
                check = se.d
            }
        }
    }

    private fun ssMergeForward(
        pa: Int, first: Int, middle: Int, last: Int, buf: Int,
        depth: Int
    ) {
        val arr = sa
        val bufEnd = buf + middle - first - 1
        ssBlockSwap(buf, first, middle - first)
        var a = first
        var b = buf
        var c = middle
        val t = arr[a]
        while (true) {
            val r = ssCompare(pa + arr[b], pa + arr[c], depth)
            if (r < 0) {
                do {
                    arr[a++] = arr[b]
                    if (bufEnd <= b) {
                        arr[bufEnd] = t
                        return
                    }
                    arr[b++] = arr[a]
                } while (arr[b] < 0)
            } else if (r > 0) {
                do {
                    arr[a++] = arr[c]
                    arr[c++] = arr[a]
                    if (last <= c) {
                        while (b < bufEnd) {
                            arr[a++] = arr[b]
                            arr[b++] = arr[a]
                        }
                        arr[a] = arr[b]
                        arr[b] = t
                        return
                    }
                } while (arr[c] < 0)
            } else {
                arr[c] = arr[c].inv()
                do {
                    arr[a++] = arr[b]
                    if (bufEnd <= b) {
                        arr[bufEnd] = t
                        return
                    }
                    arr[b++] = arr[a]
                } while (arr[b] < 0)
                do {
                    arr[a++] = arr[c]
                    arr[c++] = arr[a]
                    if (last <= c) {
                        while (b < bufEnd) {
                            arr[a++] = arr[b]
                            arr[b++] = arr[a]
                        }
                        arr[a] = arr[b]
                        arr[b] = t
                        return
                    }
                } while (arr[c] < 0)
            }
        }
    }

    private fun ssMergeBackward(
        pa: Int, first: Int, middle: Int, last: Int, buf: Int,
        depth: Int
    ) {
        val arr = sa
        val bufEnd = buf + last - middle - 1
        ssBlockSwap(buf, middle, last - middle)
        var x = 0
        var p1: Int
        var p2: Int
        if (arr[bufEnd] < 0) {
            p1 = pa + arr[bufEnd].inv()
            x = x or 1
        } else p1 = pa + arr[bufEnd]
        if (arr[middle - 1] < 0) {
            p2 = pa + arr[middle - 1].inv()
            x = x or 2
        } else p2 = pa + arr[middle - 1]
        var a = last - 1
        var b = bufEnd
        var c = middle - 1
        val t = arr[a]
        while (true) {
            val r = this.ssCompare(p1, p2, depth)
            if (r > 0) {
                if (x and 1 != 0) {
                    do {
                        arr[a--] = arr[b]
                        arr[b--] = arr[a]
                    } while (arr[b] < 0)
                    x = x xor 1
                }
                arr[a--] = arr[b]
                if (b <= buf) {
                    arr[buf] = t
                    break
                }
                arr[b--] = arr[a]
                if (arr[b] < 0) {
                    p1 = pa + arr[b].inv()
                    x = x or 1
                } else p1 = pa + arr[b]
            } else if (r < 0) {
                if (x and 2 != 0) {
                    do {
                        arr[a--] = arr[c]
                        arr[c--] = arr[a]
                    } while (arr[c] < 0)
                    x = x xor 2
                }
                arr[a--] = arr[c]
                arr[c--] = arr[a]
                if (c < first) {
                    while (buf < b) {
                        arr[a--] = arr[b]
                        arr[b--] = arr[a]
                    }
                    arr[a] = arr[b]
                    arr[b] = t
                    break
                }
                if (arr[c] < 0) {
                    p2 = pa + arr[c].inv()
                    x = x or 2
                } else p2 = pa + arr[c]
            } else  // r = 0
            {
                if (x and 1 != 0) {
                    do {
                        arr[a--] = arr[b]
                        arr[b--] = arr[a]
                    } while (arr[b] < 0)
                    x = x xor 1
                }
                arr[a--] = arr[b].inv()
                if (b <= buf) {
                    arr[buf] = t
                    break
                }
                arr[b--] = arr[a]
                if (x and 2 != 0) {
                    do {
                        arr[a--] = arr[c]
                        arr[c--] = arr[a]
                    } while (arr[c] < 0)
                    x = x xor 2
                }
                arr[a--] = arr[c]
                arr[c--] = arr[a]
                if (c < first) {
                    while (buf < b) {
                        arr[a--] = arr[b]
                        arr[b--] = arr[a]
                    }
                    arr[a] = arr[b]
                    arr[b] = t
                    break
                }
                if (arr[b] < 0) {
                    p1 = pa + arr[b].inv()
                    x = x or 1
                } else p1 = pa + arr[b]
                if (arr[c] < 0) {
                    p2 = pa + arr[c].inv()
                    x = x or 2
                } else p2 = pa + arr[c]
            }
        }
    }

    private fun ssInsertionSort(pa: Int, first: Int, last: Int, depth: Int) {
        val arr = sa
        for (i in last - 2 downTo first) {
            val t = pa + arr[i]
            var j = i + 1
            var r: Int
            while (this.ssCompare(t, pa + arr[j], depth).also { r = it } > 0) {
                do {
                    arr[j - 1] = arr[j]
                    j++
                } while (j < last && arr[j] < 0)
                if (j >= last) break
            }
            if (r == 0) arr[j] = arr[j].inv()
            arr[j - 1] = t - pa
        }
    }

    private fun ssMultiKeyIntroSort(pa: Int, first: Int, last: Int, depth: Int) {
        var first = first
        var last = last
        var depth = depth
        var limit = ssIlg(last - first)
        var x = 0
        while (true) {
            if (last - first <= SS_INSERTIONSORT_THRESHOLD) {
                if (last - first > 1) ssInsertionSort(pa, first, last, depth)
                val se = ssStack.pop() ?: return
                first = se.a
                last = se.b
                depth = se.c
                limit = se.d
                continue
            }
            val idx = depth
            if (limit == 0) ssHeapSort(idx, pa, first, last - first)
            limit--
            var a: Int
            if (limit < 0) {
                var v = buffer[idx + sa[pa + sa[first]]].toInt()
                a = first + 1
                while (a < last) {
                    if (buffer[idx + sa[pa + sa[a]]].also { x = it.toInt() }.toInt() != v) {
                        if (a - first > 1) break
                        v = x
                        first = a
                    }
                    a++
                }
                if (buffer[idx + sa[pa + sa[first]] - 1] < v) first =
                    ssPartition(pa, first, a, depth)
                if (a - first <= last - a) {
                    if (a - first > 1) {
                        ssStack.push(a, last, depth, -1, 0)
                        last = a
                        depth++
                        limit = ssIlg(a - first)
                    } else {
                        first = a
                        limit = -1
                    }
                } else {
                    if (last - a > 1) {
                        ssStack.push(first, a, depth + 1, ssIlg(a - first), 0)
                        first = a
                        limit = -1
                    } else {
                        last = a
                        depth++
                        limit = ssIlg(a - first)
                    }
                }
                continue
            }
            // choose pivot
            a = ssPivot(idx, pa, first, last)
            val v = buffer[idx + sa[pa + sa[a]]].toInt()
            swapInSA(first, a)
            var b = first
            // partition
            while (++b < last) {
                if (buffer[idx + sa[pa + sa[b]]].also { x = it.toInt() }.toInt() != v) break
            }
            a = b
            if (a < last && x < v) {
                while (++b < last) {
                    if (buffer[idx + sa[pa + sa[b]]].also { x = it.toInt() } > v) break
                    if (x == v) {
                        swapInSA(b, a)
                        a++
                    }
                }
            }
            var c = last
            while (--c > b) {
                if (buffer[idx + sa[pa + sa[c]]].also { x = it.toInt() }.toInt() != v) break
            }
            var d = c
            if (b < d && x > v) {
                while (--c > b) {
                    if (buffer[idx + sa[pa + sa[c]]].also { x = it.toInt() } < v) break
                    if (x == v) {
                        swapInSA(c, d)
                        d--
                    }
                }
            }
            while (b < c) {
                swapInSA(b, c)
                while (++b < c) {
                    if (buffer[idx + sa[pa + sa[b]]].also { x = it.toInt() } > v) break
                    if (x == v) {
                        swapInSA(b, a)
                        a++
                    }
                }
                while (--c > b) {
                    if (buffer[idx + sa[pa + sa[c]]].also { x = it.toInt() } < v) break
                    if (x == v) {
                        swapInSA(c, d)
                        d--
                    }
                }
            }
            if (a <= d) {
                c = b - 1
                var s = if (a - first > b - a) b - a else a - first
                run {
                    var e = first
                    var f = b - s
                    while (s > 0) {
                        this.swapInSA(e, f)
                        s--
                        e++
                        f++
                    }
                }
                s = if (d - c > last - d - 1) last - d - 1 else d - c
                var e = b
                var f = last - s
                while (s > 0) {
                    swapInSA(e, f)
                    s--
                    e++
                    f++
                }
                a = first + (b - a)
                c = last - (d - c)
                b = if (v <= buffer[idx + sa[pa + sa[a]] - 1]) a else ssPartition(
                    pa,
                    a,
                    c,
                    depth
                )
                if (a - first <= last - c) {
                    if (last - c <= c - b) {
                        ssStack.push(b, c, depth + 1, ssIlg(c - b), 0)
                        ssStack.push(c, last, depth, limit, 0)
                        last = a
                    } else if (a - first <= c - b) {
                        ssStack.push(c, last, depth, limit, 0)
                        ssStack.push(b, c, depth + 1, ssIlg(c - b), 0)
                        last = a
                    } else {
                        ssStack.push(c, last, depth, limit, 0)
                        ssStack.push(first, a, depth, limit, 0)
                        first = b
                        last = c
                        depth++
                        limit = ssIlg(c - b)
                    }
                } else {
                    if (a - first <= c - b) {
                        ssStack.push(b, c, depth + 1, ssIlg(c - b), 0)
                        ssStack.push(first, a, depth, limit, 0)
                        first = c
                    } else if (last - c <= c - b) {
                        ssStack.push(first, a, depth, limit, 0)
                        ssStack.push(b, c, depth + 1, ssIlg(c - b), 0)
                        first = c
                    } else {
                        ssStack.push(first, a, depth, limit, 0)
                        ssStack.push(c, last, depth, limit, 0)
                        first = b
                        last = c
                        depth++
                        limit = ssIlg(c - b)
                    }
                }
            } else {
                if (buffer[idx + sa[pa + sa[first]] - 1] < v) {
                    first = ssPartition(pa, first, last, depth)
                    limit = ssIlg(last - first)
                } else {
                    limit++
                }
                depth++
            }
        }
    }

    private fun ssPivot(td: Int, pa: Int, first: Int, last: Int): Int {
        var first = first
        var last = last
        var t = last - first
        var middle = first + (t shr 1)
        if (t <= 512) {
            return if (t <= 32) ssMedian3(td, pa, first, middle, last - 1) else ssMedian5(
                td,
                pa,
                first,
                first + (t shr 2),
                middle,
                last - 1 - (t shr 2),
                last - 1
            )
        }
        t = t shr 3
        first = ssMedian3(td, pa, first, first + t, first + (t shl 1))
        middle = ssMedian3(td, pa, middle - t, middle, middle + t)
        last = ssMedian3(td, pa, last - 1 - (t shl 1), last - 1 - t, last - 1)
        return ssMedian3(td, pa, first, middle, last)
    }

    private fun ssMedian5(idx: Int, pa: Int, v1: Int, v2: Int, v3: Int, v4: Int, v5: Int): Int {
        var v1 = v1
        var v2 = v2
        var v3 = v3
        var v4 = v4
        var v5 = v5
        if (buffer[idx + sa[pa + sa[v2]]] > buffer[idx + sa[pa + sa[v3]]]
        ) {
            val t = v2
            v2 = v3
            v3 = t
        }
        if (buffer[idx + sa[pa + sa[v4]]] > buffer[idx + sa[pa + sa[v5]]]
        ) {
            val t = v4
            v4 = v5
            v5 = t
        }
        if (buffer[idx + sa[pa + sa[v2]]] > buffer[idx + sa[pa + sa[v4]]]
        ) {
            val t1 = v2
            v2 = v4
            v4 = t1
            val t2 = v3
            v3 = v5
            v5 = t2
        }
        if (buffer[idx + sa[pa + sa[v1]]] > buffer[idx + sa[pa + sa[v3]]]
        ) {
            val t = v1
            v1 = v3
            v3 = t
        }
        if (buffer[idx + sa[pa + sa[v1]]] > buffer[idx + sa[pa + sa[v4]]]
        ) {
            val t1 = v1
            v1 = v4
            v4 = t1
            val t2 = v3
            v3 = v5
            v5 = t2
        }
        return if (buffer[idx + sa[pa + sa[v3]]] > buffer[idx + sa[pa + sa[v4]]]
        ) v4 else v3
    }

    private fun ssMedian3(idx: Int, pa: Int, v1: Int, v2: Int, v3: Int): Int {
        var v1 = v1
        var v2 = v2
        if (buffer[idx + sa[pa + sa[v1]]] > buffer[idx + sa[pa + sa[v2]]]
        ) {
            val t = v1
            v1 = v2
            v2 = t
        }
        return if (buffer[idx + sa[pa + sa[v2]]] > buffer[idx + sa[pa + sa[v3]]]
        ) if (buffer[idx + sa[pa + sa[v1]]] > buffer[idx + sa[pa + sa[v3]]]
        ) v1 else v3 else v2
    }

    private fun ssPartition(pa: Int, first: Int, last: Int, depth: Int): Int {
        val arr = sa
        var a = first - 1
        var b = last
        val d = depth - 1
        val pb = pa + 1
        while (true) {
            a++
            while (a < b && arr[pa + arr[a]] + d >= arr[pb + arr[a]]) {
                arr[a] = arr[a].inv()
                a++
            }
            b--
            while (b > a && arr[pa + arr[b]] + d < arr[pb + arr[b]]) {
                b--
            }
            if (b <= a) break
            val t = arr[b].inv()
            arr[b] = arr[a]
            arr[a] = t
        }
        if (first < a) arr[first] = arr[first].inv()
        return a
    }

    private fun ssHeapSort(idx: Int, pa: Int, saIdx: Int, size: Int) {
        var m = size
        if (size and 1 == 0) {
            m--
            if (buffer[idx + sa[pa + sa[saIdx + (m shr 1)]]] < buffer[idx + sa[pa + sa[saIdx + m]]]
            ) swapInSA(saIdx + m, saIdx + (m shr 1))
        }
        for (i in (m shr 1) - 1 downTo 0) ssFixDown(idx, pa, saIdx, i, m)
        if (size and 1 == 0) {
            swapInSA(saIdx, saIdx + m)
            ssFixDown(idx, pa, saIdx, 0, m)
        }
        for (i in m - 1 downTo 1) {
            val t = sa[saIdx]
            sa[saIdx] = sa[saIdx + i]
            ssFixDown(idx, pa, saIdx, 0, i)
            sa[saIdx + i] = t
        }
    }

    private fun ssFixDown(idx: Int, pa: Int, saIdx: Int, i: Int, size: Int) {
        var i = i
        val arr = sa
        val v = arr[saIdx + i]
        val c = buffer[idx + arr[pa + v]].toInt()
        var j = (i shl 1) + 1
        while (j < size) {
            var k = j
            j++
            var d = buffer[idx + arr[pa + arr[saIdx + k]]].toInt()
            val e = buffer[idx + arr[pa + arr[saIdx + j]]].toInt()
            if (d < e) {
                k = j
                d = e
            }
            if (d <= c) break
            arr[saIdx + i] = arr[saIdx + k]
            i = k
            j = (i shl 1) + 1
        }
        arr[i + saIdx] = v
    }

    private fun swapInSA(a: Int, b: Int) {
        val tmp = sa[a]
        sa[a] = sa[b]
        sa[b] = tmp
    }

    // Tandem Repeat Sort
    private fun trSort(n: Int, depth: Int) {
        val arr = sa
        val budget = TRBudget(trIlg(n) * 2 / 3, n)
        var isad = n + depth
        while (arr[0] > -n) {
            var first = 0
            var skip = 0
            var unsorted = 0
            do {
                val t = arr[first]
                if (t < 0) {
                    first -= t
                    skip += t
                } else {
                    if (skip != 0) {
                        arr[first + skip] = skip
                        skip = 0
                    }
                    val last = arr[n + t] + 1
                    if (last - first > 1) {
                        budget.count = 0
                        trIntroSort(n, isad, first, last, budget)
                        if (budget.count != 0) unsorted += budget.count else skip = first - last
                    } else if (last - first == 1) skip = -1
                    first = last
                }
            } while (first < n)
            if (skip != 0) arr[first + skip] = skip
            if (unsorted == 0) break
            isad += isad - n
        }
    }

    private fun trPartition(isad: Int, first: Int, middle: Int, last: Int, v: Int): Long {
        var first = first
        var last = last
        var x = 0
        var b = middle
        while (b < last) {
            x = sa[isad + sa[b]]
            if (x != v) break
            b++
        }
        var a = b
        if (a < last && x < v) {
            while (++b < last && sa[isad + sa[b]].also { x = it } <= v) {
                if (x == v) {
                    swapInSA(a, b)
                    a++
                }
            }
        }
        var c = last - 1
        while (c > b) {
            x = sa[isad + sa[c]]
            if (x != v) break
            c--
        }
        var d = c
        if (b < d && x > v) {
            while (--c > b && sa[isad + sa[c]].also { x = it } >= v) {
                if (x == v) {
                    swapInSA(c, d)
                    d--
                }
            }
        }
        while (b < c) {
            swapInSA(b, c)
            while (++b < c && sa[isad + sa[b]].also { x = it } <= v) {
                if (x == v) {
                    swapInSA(a, b)
                    a++
                }
            }
            while (--c > b && sa[isad + sa[c]].also { x = it } >= v) {
                if (x == v) {
                    swapInSA(c, d)
                    d--
                }
            }
        }
        if (a <= d) {
            c = b - 1
            var s = a - first
            if (s > b - a) s = b - a
            run {
                var e = first
                var f = b - s
                while (s > 0) {
                    this.swapInSA(e, f)
                    s--
                    e++
                    f++
                }
            }
            s = d - c
            if (s >= last - d) s = last - d - 1
            var e = b
            var f = last - s
            while (s > 0) {
                swapInSA(e, f)
                s--
                e++
                f++
            }
            first += b - a
            last -= d - c
        }
        return first.toLong() shl 32 or (last.toLong() and 0xFFFFFFFFL)
    }

    private fun trIntroSort(isa: Int, isad: Int, first: Int, last: Int, budget: TRBudget) {
        var isad = isad
        var first = first
        var last = last
        val incr = isad - isa
        val arr = sa
        var limit = trIlg(last - first)
        var trlink = -1
        while (true) {
            if (limit < 0) {
                if (limit == -1) { // tandem repeat partition
                    val res = trPartition(isad - incr, first, first, last, last - 1)
                    val a = (res shr 32).toInt()
                    val b = res.toInt()
                    // update ranks
                    if (a < last) {
                        var c = first
                        val v = a - 1
                        while (c < a) {
                            arr[isa + arr[c]] = v
                            c++
                        }
                    }
                    if (b < last) {
                        var c = a
                        val v = b - 1
                        while (c < b) {
                            arr[isa + arr[c]] = v
                            c++
                        }
                    }
                    // push
                    if (b - a > 1) {
                        trStack.push(0, a, b, 0, 0)
                        trStack.push(isad - incr, first, last, -2, trlink)
                        trlink = trStack.size() - 2
                    }
                    if (a - first <= last - b) {
                        if (a - first > 1) {
                            trStack.push(isad, b, last, trIlg(last - b), trlink)
                            last = a
                            limit = trIlg(a - first)
                        } else if (last - b > 1) {
                            first = b
                            limit = trIlg(last - b)
                        } else {
                            val se = trStack.pop() ?: return
                            isad = se.a
                            first = se.b
                            last = se.c
                            limit = se.d
                            trlink = se.e
                        }
                    } else {
                        if (last - b > 1) {
                            trStack.push(isad, first, a, trIlg(a - first), trlink)
                            first = b
                            limit = trIlg(last - b)
                        } else if (a - first > 1) {
                            last = a
                            limit = trIlg(a - first)
                        } else {
                            val se = trStack.pop() ?: return
                            isad = se.a
                            first = se.b
                            last = se.c
                            limit = se.d
                            trlink = se.e
                        }
                    }
                } else if (limit == -2) { // tandem repeat copy
                    var se = trStack.pop()
                    if (se!!.d == 0) {
                        trCopy(isa, first, se.b, se.c, last, isad - isa)
                    } else {
                        if (trlink >= 0) trStack[trlink]!!.d = -1
                        trPartialCopy(isa, first, se.b, se.c, last, isad - isa)
                    }
                    se = trStack.pop()
                    if (se == null) return
                    isad = se.a
                    first = se.b
                    last = se.c
                    limit = se.d
                    trlink = se.e
                } else { // sorted partition
                    if (arr[first] >= 0) {
                        var a = first
                        do {
                            arr[isa + arr[a]] = a
                            a++
                        } while (a < last && arr[a] >= 0)
                        first = a
                    }
                    if (first < last) {
                        var a = first
                        do {
                            arr[a] = arr[a].inv()
                            a++
                        } while (arr[a] < 0)
                        val next =
                            if (arr[isa + arr[a]] != arr[isad + arr[a]]) trIlg(a - first + 1) else -1
                        a++
                        if (a < last) {
                            val v = a - 1
                            for (b in first until a) arr[isa + arr[b]] = v
                        }
                        // push
                        if (budget.check(a - first) == true) {
                            if (a - first <= last - a) {
                                trStack.push(isad, a, last, -3, trlink)
                                isad += incr
                                last = a
                                limit = next
                            } else {
                                if (last - a > 1) {
                                    trStack.push(isad + incr, first, a, next, trlink)
                                    first = a
                                    limit = -3
                                } else {
                                    isad += incr
                                    last = a
                                    limit = next
                                }
                            }
                        } else {
                            if (trlink >= 0) trStack[trlink]!!.d = -1
                            if (last - a > 1) {
                                first = a
                                limit = -3
                            } else {
                                val se = trStack.pop() ?: return
                                isad = se.a
                                first = se.b
                                last = se.c
                                limit = se.d
                                trlink = se.e
                            }
                        }
                    } else {
                        val se = trStack.pop() ?: return
                        isad = se.a
                        first = se.b
                        last = se.c
                        limit = se.d
                        trlink = se.e
                    }
                }
                continue
            }
            if (last - first <= TR_INSERTIONSORT_THRESHOLD) {
                trInsertionSort(isad, first, last)
                limit = -3
                continue
            }
            if (limit == 0) {
                trHeapSort(isad, first, last - first)
                var a = last - 1
                while (first < a) {
                    var b = a - 1
                    val x = arr[isad + arr[a]]
                    while (first <= b && arr[isad + arr[b]] == x) {
                        arr[b] = arr[b].inv()
                        b--
                    }
                    a = b
                }
                limit = -3
                continue
            }
            limit--
            // choose pivot
            swapInSA(first, trPivot(sa, isad, first, last))
            var v = arr[isad + arr[first]]
            // partition
            val res = trPartition(isad, first, first + 1, last, v)
            val a = (res shr 32).toInt()
            val b = (res and 0xFFFFFFFFL).toInt()
            if (last - first != b - a) {
                val next = if (arr[isa + arr[a]] != v) trIlg(b - a) else -1
                v = a - 1
                // update ranks
                for (c in first until a) arr[isa + arr[c]] = v
                if (b < last) {
                    v = b - 1
                    for (c in a until b) arr[isa + arr[c]] = v
                }
                // push
                if (b - a > 1 && budget.check(b - a) == true) {
                    if (a - first <= last - b) {
                        if (last - b <= b - a) {
                            if (a - first > 1) {
                                trStack.push(isad + incr, a, b, next, trlink)
                                trStack.push(isad, b, last, limit, trlink)
                                last = a
                            } else if (last - b > 1) {
                                trStack.push(isad + incr, a, b, next, trlink)
                                first = b
                            } else {
                                isad += incr
                                first = a
                                last = b
                                limit = next
                            }
                        } else if (a - first <= b - a) {
                            if (a - first > 1) {
                                trStack.push(isad, b, last, limit, trlink)
                                trStack.push(isad + incr, a, b, next, trlink)
                                last = a
                            } else {
                                trStack.push(isad, b, last, limit, trlink)
                                isad += incr
                                first = a
                                last = b
                                limit = next
                            }
                        } else {
                            trStack.push(isad, b, last, limit, trlink)
                            trStack.push(isad, first, a, limit, trlink)
                            isad += incr
                            first = a
                            last = b
                            limit = next
                        }
                    } else {
                        if (a - first <= b - a) {
                            if (last - b > 1) {
                                trStack.push(isad + incr, a, b, next, trlink)
                                trStack.push(isad, first, a, limit, trlink)
                                first = b
                            } else if (a - first > 1) {
                                trStack.push(isad + incr, a, b, next, trlink)
                                last = a
                            } else {
                                isad += incr
                                first = a
                                last = b
                                limit = next
                            }
                        } else if (last - b <= b - a) {
                            if (last - b > 1) {
                                trStack.push(isad, first, a, limit, trlink)
                                trStack.push(isad + incr, a, b, next, trlink)
                                first = b
                            } else {
                                trStack.push(isad, first, a, limit, trlink)
                                isad += incr
                                first = a
                                last = b
                                limit = next
                            }
                        } else {
                            trStack.push(isad, first, a, limit, trlink)
                            trStack.push(isad, b, last, limit, trlink)
                            isad += incr
                            first = a
                            last = b
                            limit = next
                        }
                    }
                } else {
                    if (b - a > 1 && trlink >= 0) trStack[trlink]!!.d = -1
                    if (a - first <= last - b) {
                        if (a - first > 1) {
                            trStack.push(isad, b, last, limit, trlink)
                            last = a
                        } else if (last - b > 1) {
                            first = b
                        } else {
                            val se = trStack.pop() ?: return
                            isad = se.a
                            first = se.b
                            last = se.c
                            limit = se.d
                            trlink = se.e
                        }
                    } else {
                        if (last - b > 1) {
                            trStack.push(isad, first, a, limit, trlink)
                            first = b
                        } else if (a - first > 1) {
                            last = a
                        } else {
                            val se = trStack.pop() ?: return
                            isad = se.a
                            first = se.b
                            last = se.c
                            limit = se.d
                            trlink = se.e
                        }
                    }
                }
            } else {
                if (budget.check(last - first) == true) {
                    limit = trIlg(last - first)
                    isad += incr
                } else {
                    if (trlink >= 0) trStack[trlink]!!.d = -1
                    val se = trStack.pop() ?: return
                    isad = se.a
                    first = se.b
                    last = se.c
                    limit = se.d
                    trlink = se.e
                }
            }
        }
    }

    private fun trHeapSort(isad: Int, saIdx: Int, size: Int) {
        val arr = sa
        var m = size
        if (size and 1 == 0) {
            m--
            if (arr[isad + arr[saIdx + (m shr 1)]] < arr[isad + arr[saIdx + m]]) swapInSA(
                saIdx + m,
                saIdx + (m shr 1)
            )
        }
        for (i in (m shr 1) - 1 downTo 0) trFixDown(isad, saIdx, i, m)
        if (size and 1 == 0) {
            swapInSA(saIdx, saIdx + m)
            trFixDown(isad, saIdx, 0, m)
        }
        for (i in m - 1 downTo 1) {
            val t = arr[saIdx]
            arr[saIdx] = arr[saIdx + i]
            trFixDown(isad, saIdx, 0, i)
            arr[saIdx + i] = t
        }
    }

    private fun trFixDown(isad: Int, saIdx: Int, i: Int, size: Int) {
        var i = i
        val arr = sa
        val v = arr[saIdx + i]
        val c = arr[isad + v]
        var j = (i shl 1) + 1
        while (j < size) {
            var k = j
            j++
            var d = arr[isad + arr[saIdx + k]]
            val e = arr[isad + arr[saIdx + j]]
            if (d < e) {
                k = j
                d = e
            }
            if (d <= c) break
            arr[saIdx + i] = arr[saIdx + k]
            i = k
            j = (i shl 1) + 1
        }
        arr[saIdx + i] = v
    }

    private fun trInsertionSort(isad: Int, first: Int, last: Int) {
        val arr = sa
        for (a in first + 1 until last) {
            var b = a - 1
            val t = arr[a]
            var r = arr[isad + t] - arr[isad + arr[b]]
            while (r < 0) {
                do {
                    arr[b + 1] = arr[b]
                    b--
                } while (b >= first && arr[b] < 0)
                if (b < first) break
                r = arr[isad + t] - arr[isad + arr[b]]
            }
            if (r == 0) arr[b] = arr[b].inv()
            arr[b + 1] = t
        }
    }

    private fun trPartialCopy(isa: Int, first: Int, a: Int, b: Int, last: Int, depth: Int) {
        val arr = sa
        val v = b - 1
        var lastRank = -1
        var newRank = -1
        var d = a - 1
        for (c in first..d) {
            val s = arr[c] - depth
            if (s >= 0 && arr[isa + s] == v) {
                d++
                arr[d] = s
                val rank = arr[isa + s + depth]
                if (lastRank != rank) {
                    lastRank = rank
                    newRank = d
                }
                arr[isa + s] = newRank
            }
        }
        lastRank = -1
        run {
            var e = d
            while (first <= e) {
                val rank = arr[isa + arr[e]]
                if (lastRank != rank) {
                    lastRank = rank
                    newRank = e
                }
                if (newRank != rank) {
                    arr[isa + arr[e]] = newRank
                }
                e--
            }
        }
        lastRank = -1
        val e = d + 1
        d = b
        var c = last - 1
        while (d > e) {
            val s = arr[c] - depth
            if (s >= 0 && arr[isa + s] == v) {
                d--
                arr[d] = s
                val rank = arr[isa + s + depth]
                if (lastRank != rank) {
                    lastRank = rank
                    newRank = d
                }
                arr[isa + s] = newRank
            }
            c--
        }
    }

    private fun trCopy(isa: Int, first: Int, a: Int, b: Int, last: Int, depth: Int) {
        val arr = sa
        val v = b - 1
        var d = a - 1
        for (c in first..d) {
            val s = arr[c] - depth
            if (s >= 0 && arr[isa + s] == v) {
                d++
                arr[d] = s
                arr[isa + s] = d
            }
        }
        val e = d + 1
        d = b
        var c = last - 1
        while (d > e) {
            val s = arr[c] - depth
            if (s >= 0 && arr[isa + s] == v) {
                d--
                arr[d] = s
                arr[isa + s] = d
            }
            c--
        }
    }

    private class StackElement {
        var a = 0
        var b = 0
        var c = 0
        var d = 0
        var e = 0
    }

    // A stack of pre-allocated elements
    private class Stack internal constructor(size: Int) {
        private val array: Array<StackElement?>
        var index = 0
        operator fun get(idx: Int): StackElement? {
            return array[idx]
        }

        fun size(): Int {
            return index
        }

        fun push(a: Int, b: Int, c: Int, d: Int, e: Int) {
            val elt = array[index]
            elt!!.a = a
            elt.b = b
            elt.c = c
            elt.d = d
            elt.e = e
            index++
        }

        fun pop(): StackElement? {
            return if (index == 0) null else array[--index]
        }

        init {
            array = arrayOfNulls(size)
            for (i in 0 until size) array[i] = StackElement()
        }
    }

    private class TRBudget(var chance: Int, var remain: Int) {
        var incVal: Int
        var count = 0
        fun check(size: Int): Boolean {
            if (size <= remain) {
                remain -= size
                return true
            }
            if (chance == 0) {
                count += size
                return false
            }
            remain += incVal - size
            chance--
            return true
        }

        init {
            incVal = remain
        }
    }

    companion object {
        private const val SS_INSERTIONSORT_THRESHOLD = 8
        private const val SS_BLOCKSIZE = 1024
        private const val SS_MISORT_STACKSIZE = 16
        private const val SS_SMERGE_STACKSIZE = 32
        private const val TR_STACKSIZE = 64
        private const val TR_INSERTIONSORT_THRESHOLD = 8
        private val SQQ_TABLE = intArrayOf(
            0, 16, 22, 27, 32, 35, 39, 42, 45, 48, 50, 53, 55, 57, 59, 61,
            64, 65, 67, 69, 71, 73, 75, 76, 78, 80, 81, 83, 84, 86, 87, 89,
            90, 91, 93, 94, 96, 97, 98, 99, 101, 102, 103, 104, 106, 107, 108, 109,
            110, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126,
            128, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142,
            143, 144, 144, 145, 146, 147, 148, 149, 150, 150, 151, 152, 153, 154, 155, 155,
            156, 157, 158, 159, 160, 160, 161, 162, 163, 163, 164, 165, 166, 167, 167, 168,
            169, 170, 170, 171, 172, 173, 173, 174, 175, 176, 176, 177, 178, 178, 179, 180,
            181, 181, 182, 183, 183, 184, 185, 185, 186, 187, 187, 188, 189, 189, 190, 191,
            192, 192, 193, 193, 194, 195, 195, 196, 197, 197, 198, 199, 199, 200, 201, 201,
            202, 203, 203, 204, 204, 205, 206, 206, 207, 208, 208, 209, 209, 210, 211, 211,
            212, 212, 213, 214, 214, 215, 215, 216, 217, 217, 218, 218, 219, 219, 220, 221,
            221, 222, 222, 223, 224, 224, 225, 225, 226, 226, 227, 227, 228, 229, 229, 230,
            230, 231, 231, 232, 232, 233, 234, 234, 235, 235, 236, 236, 237, 237, 238, 238,
            239, 240, 240, 241, 241, 242, 242, 243, 243, 244, 244, 245, 245, 246, 246, 247,
            247, 248, 248, 249, 249, 250, 250, 251, 251, 252, 252, 253, 253, 254, 254, 255
        )
        private val LOG_TABLE = intArrayOf(
            -1, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
            4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
        )

        private fun getIndex(a: Int): Int {
            return if (a >= 0) a else a.inv()
        }

        private fun ssIsqrt(x: Int): Int {
            if (x >= SS_BLOCKSIZE * SS_BLOCKSIZE) return SS_BLOCKSIZE
            val e =
                if (x and -0x10000 != 0) if (x and -0x1000000 != 0) 24 + LOG_TABLE[x shr 24 and 0xFF] else 16 + LOG_TABLE[x shr 16 and 0xFF] else if (x and 0x0000FF00 != 0) 8 + LOG_TABLE[x shr 8 and 0xFF] else LOG_TABLE[x and 0xFF]
            if (e < 8) return SQQ_TABLE[x] shr 4
            var y: Int
            if (e >= 16) {
                y = SQQ_TABLE[x shr e - 6 - (e and 1)] shl (e shr 1) - 7
                if (e >= 24) {
                    y = y + 1 + x / y shr 1
                }
                y = y + 1 + x / y shr 1
            } else {
                y = (SQQ_TABLE[x shr e - 6 - (e and 1)] shr 7 - (e shr 1)) + 1
            }
            return if (x < y * y) y - 1 else y
        }

        private fun ssIlg(n: Int): Int {
            return if (n and 0xFF00 != 0) 8 + LOG_TABLE[n shr 8 and 0xFF] else LOG_TABLE[n and 0xFF]
        }

        private fun trPivot(arr: IntArray, isad: Int, first: Int, last: Int): Int {
            var first = first
            var last = last
            var t = last - first
            var middle = first + (t shr 1)
            if (t <= 512) {
                if (t <= 32) return trMedian3(arr, isad, first, middle, last - 1)
                t = t shr 2
                return trMedian5(arr, isad, first, first + t, middle, last - 1 - t, last - 1)
            }
            t = t shr 3
            first = trMedian3(arr, isad, first, first + t, first + (t shl 1))
            middle = trMedian3(arr, isad, middle - t, middle, middle + t)
            last = trMedian3(arr, isad, last - 1 - (t shl 1), last - 1 - t, last - 1)
            return trMedian3(arr, isad, first, middle, last)
        }

        private fun trMedian5(arr: IntArray, isad: Int, v1: Int, v2: Int, v3: Int, v4: Int, v5: Int): Int {
            var v1 = v1
            var v2 = v2
            var v3 = v3
            var v4 = v4
            var v5 = v5
            if (arr[isad + arr[v2]] > arr[isad + arr[v3]]) {
                val t = v2
                v2 = v3
                v3 = t
            }
            if (arr[isad + arr[v4]] > arr[isad + arr[v5]]) {
                val t = v4
                v4 = v5
                v5 = t
            }
            if (arr[isad + arr[v2]] > arr[isad + arr[v4]]) {
                val t1 = v2
                v2 = v4
                v4 = t1
                val t2 = v3
                v3 = v5
                v5 = t2
            }
            if (arr[isad + arr[v1]] > arr[isad + arr[v3]]) {
                val t = v1
                v1 = v3
                v3 = t
            }
            if (arr[isad + arr[v1]] > arr[isad + arr[v4]]) {
                val t1 = v1
                v1 = v4
                v4 = t1
                val t2 = v3
                v3 = v5
                v5 = t2
            }
            return if (arr[isad + arr[v3]] > arr[isad + arr[v4]]) v4 else v3
        }

        private fun trMedian3(arr: IntArray, isad: Int, v1: Int, v2: Int, v3: Int): Int {
            var v1 = v1
            var v2 = v2
            if (arr[isad + arr[v1]] > arr[isad + arr[v2]]) {
                val t = v1
                v1 = v2
                v2 = t
            }
            return if (arr[isad + arr[v2]] > arr[isad + arr[v3]]) if (arr[isad + arr[v1]] > arr[isad + arr[v3]]
            ) v1 else v3 else v2
        }

        private fun trIlg(n: Int): Int {
            return if (n and -0x10000 != 0) if (n and -0x1000000 != 0) 24 + LOG_TABLE[n shr 24 and 0xFF] else 16 + LOG_TABLE[n shr 16 and 0xFF] else if (n and 0x0000FF00 != 0) 8 + LOG_TABLE[n shr 8 and 0xFF] else LOG_TABLE[n and 0xFF]
        }
    }

    init {
        sa = IntArray(0)
        buffer = ShortArray(0)
        ssStack = Stack(SS_MISORT_STACKSIZE)
        trStack = Stack(TR_STACKSIZE)
        mergeStack = Stack(SS_SMERGE_STACKSIZE)
    }
}

private infix fun Short.shl(i: Int): Short {
    return this.shl(i)
}
