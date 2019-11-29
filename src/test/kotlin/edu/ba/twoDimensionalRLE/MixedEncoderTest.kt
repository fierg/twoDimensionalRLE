package edu.ba.twoDimensionalRLE

import edu.ba.twoDimensionalRLE.encoder.mixed.MixedEncoder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class MixedEncoderTest {


    companion object {
        private val encoder = MixedEncoder()
        private const val fileToEncodeSmall = "testFile_small.txt"
        private const val fileToEncode = "t8.shakespeare.txt"
        private const val encodeFolder = "data/encoded/mixed"
        private const val decodeFolder = "data/decoded/mixed"
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

    @Test
    @Order(2)
    fun encodeFileSmall() {
        encoder.encodeInternal("data/${fileToEncodeSmall}", "${encodeFolder}/${fileToEncodeSmall}")
    }

    @Test
    @Order(3)
    fun encodeFile() {
        encoder.encodeInternal("data/${fileToEncode}", "${encodeFolder}/${fileToEncode}")
    }
}