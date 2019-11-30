package edu.ba.twoDimensionalRLE

import de.jupf.staticlog.Log
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class MappingTest {

    private var log = Log.kotlinInstance()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    companion object {
        private const val fileToEncodeSmall = "testFile_small.txt"
        private const val fileToEncode = "t8.shakespeare_medium.txt"
        private const val encodeFolder = "data/encoded/mapping"
        private const val decodeFolder = "data/decoded/mapping"
    }

    @Test
    @Order(1)
    fun cleanup() {
        if (File(encodeFolder).exists()) {
            File(encodeFolder).deleteRecursively()
            File(decodeFolder).deleteRecursively()
        }
        File(encodeFolder).mkdirs()
        File(decodeFolder).mkdirs()
    }


}