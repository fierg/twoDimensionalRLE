package edu.ba.twoDimensionalRLE

import edu.ba.twoDimensionalRLE.encoder.StringRunLengthEncoder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class StringRunLengthEncoderTest {

    private val strRLE = StringRunLengthEncoder()

    @Test
    @Order(1)
    fun assertEquality() {
        assert(strRLE.runLengthEncodingString("TTESSST") == "2 T 1 E 3 S 1 T")
        assert(strRLE.runLengthEncodingString("WWWWWWWWWWWWBWWWWWWWWWWWWBBBWWWWWWWWWWWWWWWWWWWWWWWWBWWWWWWWWWWWWWW") == "12 W 1 B 12 W 3 B 24 W 1 B 14 W")
    }

    @Test
    @Order(2)
    fun encodeStringRLE_short() {
        strRLE.encode("data/testFile_small2.txt")
    }

    @Test
    @Order(3)
    fun encodeStringRLE_long() {
        strRLE.encode("data/t8.shakespeare.txt")
    }

    @Test
    @Order(4)
    fun decodeStringRLE_short() {
        strRLE.decode("data/encoded/t8.shakespeare_rle.txt")
    }

    @Test
    @Order(5)
    fun decodeStringRLE_long() {
        strRLE.decode("data/encoded/testFile_small2_rle.txt")
    }


}
