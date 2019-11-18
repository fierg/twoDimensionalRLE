package edu.ba.twoDimensionalRLE.extensions

import java.util.*

fun StringBuffer.toBitSet(): BitSet {
    require(this.toString().matches(Regex("([01]+)")))

    val result = BitSet()
    this.forEachIndexed { index, char ->
        if (char == "1"[0]) result.set(index, true)
        else if (char == "0"[0]) result.set(index, false)
    }
    return result
}

