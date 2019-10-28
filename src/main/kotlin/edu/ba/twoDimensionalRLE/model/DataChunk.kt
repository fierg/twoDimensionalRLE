package edu.ba.twoDimensionalRLE.model

import edu.ba.twoDimensionalRLE.extensions.pow
import kotlin.experimental.and
import kotlin.experimental.or

class DataChunk(val bytes: ByteArray) {

    fun getLineFromChunk(line: Int, bitSize: Int): ByteArray {
        assert(bitSize > line)
        val noOfChars = bytes.size / bitSize.toDouble()
        assert(isWhole(noOfChars))

        val chars = ByteArray(noOfChars.toInt())
        var byteCounter = 0
        bytes.forEachIndexed { index, byte ->
            if (index != 0 && index % bitSize == 0)
                byteCounter++
            if (byte.and(2.pow(line).toByte()) == 2.pow(line).toByte())
                chars[byteCounter] = chars[byteCounter].or(2.pow(index % bitSize).toByte())
        }
        return chars
    }

    private fun isWhole(value: Double): Boolean {
        return value - value.toInt() == 0.0
    }
}