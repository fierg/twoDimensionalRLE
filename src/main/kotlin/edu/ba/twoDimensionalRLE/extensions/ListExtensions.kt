package edu.ba.twoDimensionalRLE.extensions


fun List<Boolean>.toByteArray(): ByteArray {
    var result = ByteArray(0)

    this.chunked(8).forEach { chunk ->
        var chunkValue = 0
        chunk.forEachIndexed { index , bit ->
            if (bit) {
                chunkValue += 2.pow(index)
            }
        }

        result += chunkValue.toByte()
    }
    return result
}