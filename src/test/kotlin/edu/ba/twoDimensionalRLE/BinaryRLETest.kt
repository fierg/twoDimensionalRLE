package edu.ba.twoDimensionalRLE

import edu.ba.twoDimensionalRLE.encoder.BinaryRunLengthEncoder
import org.junit.Test

class BinaryRLETest {

    private val binaryRunLengthEncoder = BinaryRunLengthEncoder()

    @Test
    fun encodeFile() {
        binaryRunLengthEncoder.encode("data/t8.shakespeare.txt")
    }

    @Test
    fun decodeFile() {
        binaryRunLengthEncoder.decode("data/encoded/t8.shakespeare.txt_bin_rle_nr")
    }
}