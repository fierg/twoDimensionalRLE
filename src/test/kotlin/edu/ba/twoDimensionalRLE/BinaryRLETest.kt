package edu.ba.twoDimensionalRLE

import edu.ba.twoDimensionalRLE.encoder.BinaryRunLengthEncoder
import org.junit.Test

class BinaryRLETest {

    @Test
    fun encodeFile(){
        val binaryRunLengthEncoder = BinaryRunLengthEncoder()
        binaryRunLengthEncoder.encode("data/t8.shakespeare.txgit t")
    }
}