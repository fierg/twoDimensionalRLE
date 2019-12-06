package edu.ba.twoDimensionalRLE.extensions

fun IntRange.getSize(): Int {
    if (this.first >= 0 && this.last >= 0) return this.last - this.first + 1
    return 0
}