package edu.ba.twoDimensionalRLE

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.encoder.mixed.MixedEncoder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File
import java.lang.IllegalArgumentException
import kotlin.test.assertFailsWith

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class MixedEncoderTest {

    private var log = Log.kotlinInstance()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    companion object {
        private val encoder = MixedEncoder()
        private const val fileToEncodeSmall = "testFile_small.txt"
        private const val fileToEncode = "t8.shakespeare.txt"
        private const val encodeFolder = "data/encoded/mixed"
        private const val decodeFolder = "data/decoded/mixed"
    }

    @Test
    @Order(1)
    fun cleanup() {
        if (File(encodeFolder).exists()) {
            File(encodeFolder).deleteRecursively()
            File(decodeFolder).deleteRecursively()
        }
        File(encodeFolder).mkdirs()
        File(decodeFolder).mkdirs()
    }

    @ExperimentalStdlibApi
    @Test
    @Order(2)
    fun encodeFileSmall() {
        encoder.encode("data/${fileToEncodeSmall}", "${encodeFolder}/${fileToEncodeSmall}")
    }

    @ExperimentalStdlibApi
    @Test
    @Order(3)
    fun encodeFile() {
        encoder.encode("data/${fileToEncode}", "${encodeFolder}/${fileToEncode}")
    }

    @ExperimentalUnsignedTypes
    @Test
    @Order(4)
    fun debugPrint() {
        encoder.debugPrintFileContent(File("${encodeFolder}/${fileToEncodeSmall}"))
    }

    @ExperimentalUnsignedTypes
    @ExperimentalStdlibApi
    @Test
    @Order(5)
    fun decodeFileSmallNoMap() {
        assertFailsWith<IllegalArgumentException> {
            encoder.readEncodedFileConsecutive(
                "${encodeFolder}/${fileToEncodeSmall}",
                256,
                log,
                MixedEncoder.RLE_BIT_RANGE,
                MixedEncoder.HUFF_BIT_RANGE,
                emptyMap()
            )
        }
    }
}