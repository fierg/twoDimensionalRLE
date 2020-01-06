package edu.ba.twoDimensionalRLE.tranformation

import edu.ba.twoDimensionalRLE.extensions.shift
import java.lang.IllegalArgumentException
import java.math.BigInteger

@ExperimentalStdlibApi
class BurrowsWheelerTransformationLinearTime {


    fun transform(input: String): Pair<String, Int> {
        val table = Array(input.length) { input.substring(it) + input.substring(0, it) }
        table.sort()
        val index = table.indexOfFirst { it == input }
        return Pair(String(table.map { it[it.lastIndex] }.toCharArray()), index)
    }


    @Deprecated("currently broken due to comparison method!")
    fun transformByteArray(input: ByteArray): Pair<ByteArray, Int> {
        val table = Array(input.size) { input.shift(it)}
        
        //TODO fix!!!!
        table.sortWith(Comparator { o1, o2 -> (BigInteger(o1) - BigInteger(o2)).toInt() })
        val index = table.indexOfFirst { it.contentEquals(input) }

        return Pair(table.map { it[it.lastIndex] }.toByteArray(), index)
    }


    fun inverseTransform(L: String, index: Int): String {

        // corresponding to D1. [find first characters of rotations]
        val F = L.toCharArray().sortedArray()

        // corresponding to D2. [build list of predecessor characters]
        val P = IntArray(L.length)
        val C = mutableMapOf<Byte, Int>()

        for (i in 0 until L.length) {
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

        for (i in 0 until L.size) {
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

        var i = index
        for (j in L.size-1 downTo 0){
            S[j] = L[i]
            i = P[i] + C.getOrElse(L[i]) {throw IllegalArgumentException("Unexpected index!")}
        }

        return S
    }
}