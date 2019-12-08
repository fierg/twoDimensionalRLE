package edu.ba.twoDimensionalRLE

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.encoder.mixed.MixedEncoder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File
import kotlin.test.assertFailsWith

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
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
        private const val fileToEncodeSmall2 = "testFile_small2.txt"
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


    @Test
    @Order(2)
    fun encodeFileSmall() {
        encoder.encode(
            "data/${fileToEncodeSmall}", "${encodeFolder}/${fileToEncodeSmall}",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(7)
    fun decodeFileSmall() {
        encoder.decode(
            "${encodeFolder}/${fileToEncodeSmall}", "${decodeFolder}/${fileToEncodeSmall}",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }


    @Test
    @Order(3)
    fun encodeFileSmall2() {
        encoder.encode(
            "data/${fileToEncodeSmall2}", "${encodeFolder}/${fileToEncodeSmall2}",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

/*
    @Test
    @Order(4)
    fun encodeFile() {
        encoder.encode(
            "data/${fileToEncode}", "${encodeFolder}/${fileToEncode}",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }*/

    @Test
    @Order(5)
    fun debugPrint() {
        encoder.debugPrintFileContent(File("${encodeFolder}/${fileToEncodeSmall}"))
    }

    @ExperimentalUnsignedTypes
    @ExperimentalStdlibApi
    @Test
    @Order(6)
    fun decodeFileSmallNoMap() {
        assertFailsWith<IllegalArgumentException> {
            encoder.readEncodedFileConsecutive(
                "${encodeFolder}/${fileToEncodeSmall}",
                256,
                log,
                MixedEncoder.BIN_RLE_BIT_RANGE,
                MixedEncoder.HUFF_BIT_RANGE,
                emptyMap()
            )
        }
    }


    @Test
    @Order(8)
    fun decodeFileSmall2() {
        encoder.decode(
            "${encodeFolder}/${fileToEncodeSmall2}", "${decodeFolder}/${fileToEncodeSmall2}",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    /*
    @Test
    @Order(8)
    fun decodeFile() {
        encoder.decode(
            "${encodeFolder}/${fileToEncode}", "${decodeFolder}/${fileToEncode}",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }
*/
}