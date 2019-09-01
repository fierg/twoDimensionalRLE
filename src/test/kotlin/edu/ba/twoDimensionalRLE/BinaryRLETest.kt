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
    fun encodeFile() {
        binaryRunLengthEncoder.encode("data/RLE_TEST_FILE.txt")
    }

    @Test
    @Order(2)
    fun decodeFile() {
        binaryRunLengthEncoder.decode("data/encoded/RLE_TEST_FILE.txt_bin_rle_nr")
    }
}