package edu.ba.twoDimensionalRLE

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.encoder.huffman2.HuffmanEncoder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import java.io.File


class HuffmanTest {
    private var log = Log.kotlinInstance()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    @Test
    @Order(1)
    fun huff1() {
        val encoder = edu.ba.twoDimensionalRLE.encoder.huffman.HuffmanEncoder()
        val mapping = encoder.getHuffmanMapping(256,"aaaabbbccddefg".toByteArray())
        mapping
    }
}