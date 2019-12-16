package edu.ba.twoDimensionalRLE.huffman

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.encoder.huffman.HuffmanEncoder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class HuffmanTest {
    private var log = Log.kotlinInstance()
    private val byteArraySize = 256

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
        encoder.encode("data/$fileToEncodeSmall", "$encodeFolder/$fileToEncodeSmall",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }


    @ExperimentalUnsignedTypes
    @Test
    @Order(5)
    fun decodeFileSmall() {
        val encoder = HuffmanEncoder()
        encoder.decode("$encodeFolder/$fileToEncodeSmall", "$decodeFolder/$fileToEncodeSmall",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }


    @ExperimentalUnsignedTypes
    @Test
    @Order(4)
    fun encodeFileLarge() {
        val encoder = HuffmanEncoder()
        encoder.encode("data/$fileToEncode", "$encodeFolder/$fileToEncode",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @ExperimentalUnsignedTypes
    @Test
    @Order(6)
    fun decodeFileLarge() {
        val encoder = HuffmanEncoder()
        encoder.decode( "$encodeFolder/$fileToEncode", "$decodeFolder/$fileToEncode",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }
}