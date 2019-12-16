package edu.ba.twoDimensionalRLE.huffman

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.encoder.huffman.HuffmanEncoder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File


@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class HuffmanCorpusTest {


    private var log = Log.kotlinInstance()
    private val byteArraySize = 256

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    companion object {
        private const val folderToEncode = "data/corpus/CalgaryCorpus"
        private const val encodeFolder = "data/encoded/huffman"
        private const val decodeFolder = "data/decoded/huffman"
    }

    private val huffmanEncoder = HuffmanEncoder()

    @Test
    @Order(1)
    fun cleanup() {
        if (File("$encodeFolder/CalgaryCorpus").exists()) {
            log.info("deleting directory: $encodeFolder/CalgaryCorpus")
            File("$encodeFolder/CalgaryCorpus").deleteRecursively()
            File("$decodeFolder/CalgaryCorpus").deleteRecursively()


        }
        log.info("creating directory: $encodeFolder/CalgaryCorpus")
        File("$encodeFolder/CalgaryCorpus").mkdirs()
        File("$decodeFolder/CalgaryCorpus").mkdirs()

    }

    @Test
    @Order(2)
    fun encodeHuffman_book1() {
        huffmanEncoder.encode("$folderToEncode/book1", "$encodeFolder/CalgaryCorpus/book1.huff",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(3)
    fun decodeHuffman_book1() {
        huffmanEncoder.decode("$encodeFolder/CalgaryCorpus/book1.huff", "$decodeFolder/CalgaryCorpus/book1",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(4)
    fun encodeHuffman_book2() {
        huffmanEncoder.encode("$folderToEncode/book2", "$encodeFolder/CalgaryCorpus/book2.huff",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(5)
    fun decodeHuffman_book2() {
        huffmanEncoder.decode("$encodeFolder/CalgaryCorpus/book2.huff", "$decodeFolder/CalgaryCorpus/book2",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(6)
    fun encodeHuffman_bib() {
        huffmanEncoder.encode("$folderToEncode/bib", "$encodeFolder/CalgaryCorpus/bib.huff",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(7)
    fun decodeHuffman_bib() {
        huffmanEncoder.decode("$encodeFolder/CalgaryCorpus/bib.huff", "$decodeFolder/CalgaryCorpus/bib",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(8)
    fun encodeHuffman_geo() {
        huffmanEncoder.encode("$folderToEncode/geo", "$encodeFolder/CalgaryCorpus/geo.huff",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(9)
    fun decodeHuffman_geo() {
        huffmanEncoder.decode("$encodeFolder/CalgaryCorpus/geo.huff", "$decodeFolder/CalgaryCorpus/geo",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(10)
    fun encodeHuffman_news() {
        huffmanEncoder.encode("$folderToEncode/news", "$encodeFolder/CalgaryCorpus/news.huff",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(11)
    fun decodeHuffman_news() {
        huffmanEncoder.decode("$encodeFolder/CalgaryCorpus/news.huff", "$decodeFolder/CalgaryCorpus/news",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(12)
    fun encodeHuffman_obj1() {
        huffmanEncoder.encode("$folderToEncode/obj1", "$encodeFolder/CalgaryCorpus/obj1.huff",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(13)
    fun decodeHuffman_obj1() {
        huffmanEncoder.decode("$encodeFolder/CalgaryCorpus/obj1.huff", "$decodeFolder/CalgaryCorpus/obj",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(14)
    fun encodeHuffman_paper1() {
        huffmanEncoder.encode("$folderToEncode/paper1", "$encodeFolder/CalgaryCorpus/paper1.huff",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(15)
    fun decodeHuffman_paper1() {
        huffmanEncoder.decode("$encodeFolder/CalgaryCorpus/paper1.huff", "$decodeFolder/CalgaryCorpus/paper1",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(16)
    fun encodeHuffman_paper2() {
        huffmanEncoder.encode("$folderToEncode/paper2", "$encodeFolder/CalgaryCorpus/paper2.huff",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(17)
    fun decodeHuffman_paper2() {
        huffmanEncoder.decode("$encodeFolder/CalgaryCorpus/paper2.huff", "$decodeFolder/CalgaryCorpus/paper2",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(18)
    fun encodeHuffman_pic() {
        huffmanEncoder.encode("$folderToEncode/pic", "$encodeFolder/CalgaryCorpus/pic.huff",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(19)
    fun decodeHuffman_pic() {
        huffmanEncoder.decode("$encodeFolder/CalgaryCorpus/pic.huff", "$decodeFolder/CalgaryCorpus/pic",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(20)
    fun encodeHuffman_progc() {
        huffmanEncoder.encode("$folderToEncode/progc", "$encodeFolder/CalgaryCorpus/progc.huff",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(21)
    fun decodeHuffman_progc() {
        huffmanEncoder.decode("$encodeFolder/CalgaryCorpus/progc.huff", "$decodeFolder/CalgaryCorpus/progc",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(22)
    fun encodeHuffman_progl() {
        huffmanEncoder.encode("$folderToEncode/progl", "$encodeFolder/CalgaryCorpus/progl.huff",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(23)
    fun decodeHuffman_progl() {
        huffmanEncoder.decode("$encodeFolder/CalgaryCorpus/progl.huff", "$decodeFolder/CalgaryCorpus/progl",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(24)
    fun encodeHuffman_progp() {
        huffmanEncoder.encode("$folderToEncode/progp", "$encodeFolder/CalgaryCorpus/progp.huff",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(25)
    fun decodeHuffman_progp() {
        huffmanEncoder.decode("$encodeFolder/CalgaryCorpus/progp.huff", "$decodeFolder/CalgaryCorpus/progp",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(26)
    fun encodeHuffman_trans() {
        huffmanEncoder.encode("$folderToEncode/trans", "$encodeFolder/CalgaryCorpus/trans.huff",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

    @Test
    @Order(27)
    fun decodeHuffman_trans() {
        huffmanEncoder.decode("$encodeFolder/CalgaryCorpus/trans.huff", "$decodeFolder/CalgaryCorpus/trans",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true, byteArraySize = byteArraySize)
    }

}