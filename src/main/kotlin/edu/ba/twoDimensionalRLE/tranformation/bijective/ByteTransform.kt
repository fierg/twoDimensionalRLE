package edu.ba.twoDimensionalRLE.tranformation.bijective

// A byte transform is an operation that takes an array of bytes as input and
// turns it into another array of bytes of the same size.
interface ByteTransform {
    // Read src.length bytes from src.array[src.index], process them and
// write them to dst.array[dst.index]. The index of each slice is updated
// with the number of bytes respectively read from and written to.
    fun forward(src: SliceByteArray, dst: SliceByteArray): Boolean

    // Read src.length bytes from src.array[src.index], process them and
// write them to dst.array[dst.index]. The index of each slice is updated
// with the number of bytes respectively read from and written to.
    fun inverse(src: SliceByteArray, dst: SliceByteArray): Boolean
}
