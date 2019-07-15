package edu.ba.twoDimensionalRLE

import edu.ba.twoDimensionalRLE.encoder.runLengthEncodingString
import org.junit.Test

class StringRunLengthEncoderTest {

    @Test
    fun assertEquality() {
        assert(runLengthEncodingString("TTESSST") == "2T1E3S1T")
        assert(runLengthEncodingString("WWWWWWWWWWWWBWWWWWWWWWWWWBBBWWWWWWWWWWWWWWWWWWWWWWWWBWWWWWWWWWWWWWW") == "12W1B12W3B24W1B14W")
    }
}
