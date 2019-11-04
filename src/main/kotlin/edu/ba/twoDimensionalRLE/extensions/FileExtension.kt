package edu.ba.twoDimensionalRLE.extensions

import edu.ba.twoDimensionalRLE.model.DataChunk
import java.io.File


fun File.readAllChunks(chunkSize: Int): MutableList<DataChunk> {

    val result = mutableListOf<DataChunk>()
    val bytes = ByteArray(chunkSize)
    var counter = 0

    this.inputStream().readBytes().forEach { byte ->
        bytes[counter++ % bytes.size] = byte
        if (counter % bytes.size == 0) {
            result.add(DataChunk(bytes))
        }
    }
    if (counter % bytes.size != 0) {
        val indexLeft = (counter % bytes.size)
        var index = 0
        val bytesLeft = ByteArray(indexLeft)

        bytes.slice(IntRange(0, (counter % bytes.size) - 1)).forEach {
            bytesLeft[index++] = it
        }
        result.add(DataChunk(bytes))
    }
    return result
}
