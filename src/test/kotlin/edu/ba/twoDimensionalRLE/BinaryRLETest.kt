package edu.ba.twoDimensionalRLE

import edu.ba.twoDimensionalRLE.encoder.BinaryRunLengthEncoder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class BinaryRLETest {

    private val binaryRunLengthEncoder = BinaryRunLengthEncoder()

    @Test
    @Order(1)
    fun encodeFile_small() {
        binaryRunLengthEncoder.encode("data/testFile_small2.txt")
    }

    @Test
    @Order(2)
    fun encodeFile() {
        binaryRunLengthEncoder.encode("data/t8.shakespeare.txt")
    }

    @Test
    @Order(3)
    fun decodeFile_small() {
        binaryRunLengthEncoder.decode("data/encoded/testFile_small2.txt_bin_rle_nr")
    }

    @Test
    @Order(4)
    fun decodeFile() {
        binaryRunLengthEncoder.decode("data/encoded/t8.shakespeare.txt_bin_rle_nr")
    }
}