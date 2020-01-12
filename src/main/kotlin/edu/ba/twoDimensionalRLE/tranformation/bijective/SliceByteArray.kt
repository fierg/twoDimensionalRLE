package edu.ba.twoDimensionalRLE.tranformation.bijective

import java.util.*

// A lightweight slice implementation for byte[]
class SliceByteArray {
    var array // array.length is the slice capacity
            : ByteArray?
    var length: Int
    var index: Int

    constructor(array: ByteArray?, idx: Int) {
        if (array == null) throw NullPointerException("The array cannot be null")
        if (idx < 0) throw NullPointerException("The index cannot be negative")
        this.array = array
        length = array.size
        index = idx
    }

    constructor(array: ByteArray? = ByteArray(0), length: Int = 0, idx: Int = 0) {
        if (array == null) throw NullPointerException("The array cannot be null")
        require(length >= 0) { "The length cannot be negative" }
        if (idx < 0) throw NullPointerException("The index cannot be negative")
        this.array = array
        this.length = length
        index = idx
    }

    override fun equals(o: Any?): Boolean {
        return try {
            if (o == null) return false
            if (this === o) return true
            val sa = o as SliceByteArray
            array == sa.array &&
                    length == sa.length &&
                    index == sa.index
        } catch (e: ClassCastException) {
            false
        }
    }

    override fun hashCode(): Int {
        return Objects.hashCode(array)
    }

    override fun toString(): String {
        val builder = StringBuilder(100)
        builder.append("[ data=")
        builder.append(String(array!!))
        builder.append(", len=")
        builder.append(length)
        builder.append(", idx=")
        builder.append(index)
        builder.append("]")
        return builder.toString()
    }

    companion object {
        fun isValid(sa: SliceByteArray?): Boolean {
            if (sa == null) return false
            if (sa.array == null) return false
            if (sa.index < 0) return false
            return if (sa.length < 0) false else sa.index <= sa.array!!.size
        }
    }
}