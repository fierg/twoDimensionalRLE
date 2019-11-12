package edu.ba.twoDimensionalRLE

import edu.ba.twoDimensionalRLE.encoder.mixed.MixedEncoder
import org.junit.jupiter.api.Test
import java.util.*

class MixedEncoderTest {


    companion object {
        private val encoder = MixedEncoder()
        private const val fileToEncodeSmall = "testFile_small.txt"

        private const val fileToEncode = "t8.shakespeare.txt"
        private const val encodeFolder = "data/encoded/mixed"
        private const val decodeFolder = "data/decoded/mixed"
    }

    @Test
    fun encodeFile() {
        encoder.encodeInternal("data/${fileToEncode}", "${encodeFolder}/${fileToEncode}")
    }
}