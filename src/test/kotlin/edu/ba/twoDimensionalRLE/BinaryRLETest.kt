package edu.ba.twoDimensionalRLE

import edu.ba.twoDimensionalRLE.encoder.BinaryRunLengthEncoder
import org.junit.jupiter.api.*
import java.io.File

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class BinaryRLETest {

    companion object {
        private val binaryRunLengthEncoder = BinaryRunLengthEncoder()
        private const val fileToEncodeSmall = "testFile_small2.txt"
        private const val fileToEncode = "t8.shakespeare.txt"

        private const val encodeFolder = "data/encoded/bin_rle"
        private const val decodeFolder = "data/decoded/bin_rle"
    }


    @Test
    @Order(1)
    fun cleanup() {
        if (File("$encodeFolder").exists()) {
            File(encodeFolder).deleteRecursively()
            File(decodeFolder).deleteRecursively()
        }
        File(encodeFolder).mkdirs()
        File(decodeFolder).mkdirs()
    }


    @Test
    @Order(2)
    fun encodeFile_small() {
        binaryRunLengthEncoder.encode("data/${fileToEncodeSmall}", "$encodeFolder/$fileToEncodeSmall.bin_rle")
    }

    @Test
    @Order(3)
    fun encodeFile() {
        binaryRunLengthEncoder.encode("data/${fileToEncode}", "$encodeFolder/$fileToEncode.bin_rle")
    }

    @Test
    @Order(4)
    fun decodeFile_small() {
        binaryRunLengthEncoder.decode("$encodeFolder/${fileToEncodeSmall}.bin_rle_nr", "$decodeFolder/$fileToEncodeSmall")
    }

    @Test
    @Order(5)
    fun decodeFile() {
        binaryRunLengthEncoder.decode("$encodeFolder/${fileToEncode}.bin_rle_nr", "$decodeFolder/$fileToEncode")
    }
}