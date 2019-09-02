package edu.ba.twoDimensionalRLE

import edu.ba.twoDimensionalRLE.encoder.StringRunLengthEncoder
import org.junit.jupiter.api.*
import java.io.File

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class StringRunLengthEncoderTest {
    companion object {
        private const val fileToEncodeSmall = "testFile_small2.txt"
        private const val fileToEncode = "t8.shakespeare.txt"
        private const val fileEncoded = "t8.shakespeare_rle.txt"

    }

    private val strRLE = StringRunLengthEncoder()

    @Test
    @Order(1)
    fun cleanup() {
        if (File("data/encoded/${fileEncoded}").exists()) {
            File("data/encoded").deleteRecursively()
            File("data/decoded").deleteRecursively()
            File("data/encoded").mkdir()
            File("data/decoded").mkdir()
        } else {
            File("data/encoded").mkdir()
            File("data/decoded").mkdir()
        }
    }

    @Test
    @Order(2)
    fun assertEquality() {
        assert(strRLE.runLengthEncodingString("TTESSST") == "2 T 1 E 3 S 1 T")
        assert(strRLE.runLengthEncodingString("WWWWWWWWWWWWBWWWWWWWWWWWWBBBWWWWWWWWWWWWWWWWWWWWWWWWBWWWWWWWWWWWWWW") == "12 W 1 B 12 W 3 B 24 W 1 B 14 W")
    }

    @Test
    @Order(3)
    fun encodeStringRLE_short() {
        strRLE.encode("data/$fileToEncodeSmall")
    }

    @Test
    @Order(4)
    fun encodeStringRLE_long() {
        strRLE.encode("data/$fileToEncode")
    }

    @Test
    @Order(5)
    fun decodeStringRLE_long() {
        strRLE.decode("data/encoded/t8.shakespeare_rle.txt")
    }

    @Test
    @Order(6)
    fun decodeStringRLE_short() {
        strRLE.decode("data/encoded/testFile_small2_rle.txt")
    }


}
