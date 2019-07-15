package edu.ba.twoDimensionalRLE

import org.junit.Test

class StringRunLengthEncoderTest {

    @Test
    fun assertEquality() {
        assert(runLengthEncoding("TTESSST") == "2T1E3S1T")
        assert(runLengthEncoding("WWWWWWWWWWWWBWWWWWWWWWWWWBBBWWWWWWWWWWWWWWWWWWWWWWWWBWWWWWWWWWWWWWW") == "12W1B12W3B24W1B14W")
    }
}
