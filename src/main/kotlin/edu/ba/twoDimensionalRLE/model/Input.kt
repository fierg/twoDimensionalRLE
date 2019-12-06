package edu.ba.twoDimensionalRLE.model

import de.jupf.staticlog.Log

class Input {

    private var log = Log.kotlinInstance()
    val chunks: List<DataChunk>
    val rleEncoded = mutableMapOf<Int,List<Int>>()
    val huffEncoded = mutableListOf<StringBuffer>()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    companion object {
        private val byteArraySize = 254
    }


    constructor(inputFile: String, isEncoded: Boolean) {
        chunks = DataChunk.readChunksFromFile(inputFile = inputFile, byteArraySize = byteArraySize, log = log)
    }
}