package edu.ba.twoDimensionalRLE

import edu.ba.twoDimensionalRLE.encoder.StringRunLengthEncoder
import org.junit.Test

class StringRunLengthEncoderTest {

    private val strRLE = StringRunLengthEncoder()

    @Test
    fun assertEquality() {
        assert(strRLE.runLengthEncodingString("TTESSST") == "2 T 1 E 3 S 1 T")
        assert(strRLE.runLengthEncodingString("WWWWWWWWWWWWBWWWWWWWWWWWWBBBWWWWWWWWWWWWWWWWWWWWWWWWBWWWWWWWWWWWWWW") == "12 W 1 B 12 W 3 B 24 W 1 B 14 W")
    }

    @Test
    fun encodeStringRLE() {
        strRLE.encode("data/RLE_TEST_FILE.txt")
    }

    @Test
    fun decodeStringRLE() {
        strRLE.decode("data/encoded/RLE_TEST_FILE_rle.txt")
    }


}
