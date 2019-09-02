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
    }



    @Test
    @Order(2)
    fun encodeFile_small() {
        binaryRunLengthEncoder.encode("data/${fileToEncodeSmall}")
    }

    @Test
    @Order(3)
    fun encodeFile() {
        binaryRunLengthEncoder.encode("data/${fileToEncode}")
    }

    @Test
    @Order(4)
    fun decodeFile_small() {
        binaryRunLengthEncoder.decode("data/encoded/${fileToEncodeSmall}_bin_rle_nr")
    }

    @Test
    @Order(5)
    fun decodeFile() {
        binaryRunLengthEncoder.decode("data/encoded/${fileToEncode}_bin_rle_nr")
    }
}