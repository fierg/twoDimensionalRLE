package edu.ba.twoDimensionalRLE

import edu.ba.twoDimensionalRLE.encoder.rle.StringRunLengthEncoder
import org.junit.jupiter.api.*
import java.io.File

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class StringRLETest {
    companion object {
        private const val fileToEncodeSmall = "testFile_small.txt"
        private const val fileToEncode = "t8.shakespeare.txt"


        private const val encodeFolder = "data/encoded/rle"
        private const val decodeFolder = "data/decoded/rle"
    }

    private val strRLE = StringRunLengthEncoder()

    @Test
    @Order(1)
    fun cleanup() {
        if (File("$encodeFolder/${fileToEncode}.rle").exists()) {
            File(encodeFolder).deleteRecursively()
            File(decodeFolder).deleteRecursively()
        }
        File(encodeFolder).mkdirs()
        File(decodeFolder).mkdirs()
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
        strRLE.encode("data/$fileToEncodeSmall", "$encodeFolder/$fileToEncodeSmall.rle")
    }

    @Test
    @Order(4)
    fun encodeStringRLE_long() {
        strRLE.encode("data/$fileToEncode", "$encodeFolder/$fileToEncode.rle")
    }

    @Test
    @Order(5)
    fun decodeStringRLE_long() {
        strRLE.decode("$encodeFolder/${fileToEncode}.rle", "$decodeFolder/$fileToEncode")
    }

    @Test
    @Order(6)
    fun decodeStringRLE_short() {
        strRLE.decode("$encodeFolder/$fileToEncodeSmall.rle", "$decodeFolder/$fileToEncodeSmall")
    }


}
