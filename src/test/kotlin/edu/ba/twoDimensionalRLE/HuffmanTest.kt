package edu.ba.twoDimensionalRLE

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.encoder.huffman.HuffmanEncoder
import edu.ba.twoDimensionalRLE.extensions.reversed
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class HuffmanTest {
    private var log = Log.kotlinInstance()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    companion object {
        private const val fileToEncodeSmall = "testFile_small2.txt"
        private const val fileToEncode = "t8.shakespeare.txt"
        private const val encodeFolder = "data/encoded/huffman"
        private const val decodeFolder = "data/decoded/huffman"
        private const val inputString = "aaaabbbccddefg"
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

    @ExperimentalUnsignedTypes
    @Test
    @Order(2)
    fun getHuffmanMapping() {
        log.info("Creating huffman mapping from String $inputString...")
        log.info(HuffmanEncoder().getHuffmanMapping(256, inputString.toByteArray()).toString())
    }


    @ExperimentalUnsignedTypes
    @Test
    @Order(3)
    fun encodeFileSmall() {
        val encoder = HuffmanEncoder()
        encoder.encode("data/${fileToEncodeSmall}", "${encodeFolder}/${fileToEncodeSmall}")
    }


    @ExperimentalUnsignedTypes
    @Test
    @Order(5)
    fun decodeFileSmall() {
        val encoder = HuffmanEncoder()
        encoder.decode("${encodeFolder}/${fileToEncodeSmall}", "${decodeFolder}/${fileToEncodeSmall}")
    }


    @ExperimentalUnsignedTypes
    @Test
    @Order(4)
    fun encodeFileLarge() {
        val encoder = HuffmanEncoder()
        encoder.encode("data/${fileToEncode}", "${encodeFolder}/${fileToEncode}")
    }

    @ExperimentalUnsignedTypes
    @Test
    @Order(6)
    fun decodeFileLarge() {
        val encoder = HuffmanEncoder()
        encoder.decode( "${encodeFolder}/${fileToEncode}", "${decodeFolder}/${fileToEncode}")
    }
}