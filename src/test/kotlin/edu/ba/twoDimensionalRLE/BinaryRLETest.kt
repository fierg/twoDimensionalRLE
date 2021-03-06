package edu.ba.twoDimensionalRLE

import edu.ba.twoDimensionalRLE.encoder.rle.BinaryRunLengthEncoder
import org.junit.jupiter.api.*
import java.io.File

@ExperimentalUnsignedTypes
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class BinaryRLETest {
    private val byteArraySize = 256
    private val bitsPerRLENumber = 0

    companion object {
        private val binaryRunLengthEncoder = BinaryRunLengthEncoder()
        private const val fileToEncodeSmall = "testFile_small.txt"
        private const val fileToEncode = "t8.shakespeare.txt"

        private const val encodeFolder = "data/encoded/bin_rle"
        private const val decodeFolder = "data/decoded/bin_rle"
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
    fun encodeFile_small() {
        binaryRunLengthEncoder.encode("data/${fileToEncodeSmall}", "$encodeFolder/$fileToEncodeSmall.bin_rle",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        , byteArraySize = byteArraySize, bitsPerRLENumber =bitsPerRLENumber
        )
    }


    @Test
    @Order(4)
    fun decodeFile_small() {
        binaryRunLengthEncoder.decode(
            "$encodeFolder/${fileToEncodeSmall}.bin_rle_nr",
            "$decodeFolder/$fileToEncodeSmall",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize, bitsPerRLENumber =bitsPerRLENumber
        )
    }


    @Test
    @Order(7)
    fun encodeAndDecodeFileMapped() {
        val mapping = binaryRunLengthEncoder.encodeMapped("data/${fileToEncode}", "$encodeFolder/${fileToEncode}.mp", byteArraySize = byteArraySize)
        binaryRunLengthEncoder.decodeMapped(
            "$encodeFolder/$fileToEncode.mp_nr",
            "$decodeFolder/${fileToEncode}.mp",
            mapping, byteArraySize = byteArraySize
        )

    }

    @Test
    @Order(8)
    fun encodeAndDecodeFileMappedSmall() {
        val mapping = binaryRunLengthEncoder.encodeMapped("data/${fileToEncodeSmall}", "$encodeFolder/${fileToEncodeSmall}.mp", byteArraySize = byteArraySize)
        binaryRunLengthEncoder.decodeMapped(
            "$encodeFolder/$fileToEncodeSmall.mp_nr",
            "$decodeFolder/${fileToEncodeSmall}.mp",
            mapping, byteArraySize = byteArraySize
        )

    }
}