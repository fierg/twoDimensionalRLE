package edu.ba.twoDimensionalRLE

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.encoder.mixed.MixedEncoder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class MixedEncoderCorpusTest {


    private var log = Log.kotlinInstance()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    companion object {
        private const val folderToEncode = "data/corpus/CalgaryCorpus"
        private const val encodeFolder = "data/encoded/mixed"
        private const val decodeFolder = "data/decoded/mixed"
    }

    private val mixedEncoder = MixedEncoder()

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
    fun encodeMixed_book1() {
        mixedEncoder.encode("$folderToEncode/book1", "$encodeFolder/CalgaryCorpus/book1.rle")
    }

    @Test
    @Order(3)
    fun decodeMixed_book1() {
        mixedEncoder.decode("$encodeFolder/CalgaryCorpus/book1.rle", "${decodeFolder}/CalgaryCorpus/book1")
    }

    @Test
    @Order(4)
    fun encodeMixed_book2() {
        mixedEncoder.encode("$folderToEncode/book2", "$encodeFolder/CalgaryCorpus/book2.rle")
    }

    @Test
    @Order(5)
    fun decodeMixed_book2() {
        mixedEncoder.decode("$encodeFolder/CalgaryCorpus/book2.rle", "${decodeFolder}/CalgaryCorpus/book2")
    }

    @Test
    @Order(6)
    fun encodeMixed_bib() {
        mixedEncoder.encode("$folderToEncode/bib", "$encodeFolder/CalgaryCorpus/bib.rle")
    }

    @Test
    @Order(7)
    fun decodeMixed_bib() {
        mixedEncoder.decode("$encodeFolder/CalgaryCorpus/bib.rle", "${decodeFolder}/CalgaryCorpus/bib")
    }

    @Test
    @Order(8)
    fun encodeMixed_geo() {
        mixedEncoder.encode("$folderToEncode/geo", "$encodeFolder/CalgaryCorpus/geo.rle")
    }

    @Test
    @Order(9)
    fun decodeMixed_geo() {
        mixedEncoder.decode("$encodeFolder/CalgaryCorpus/geo.rle", "${decodeFolder}/CalgaryCorpus/geo")
    }

    @Test
    @Order(10)
    fun encodeMixed_news() {
        mixedEncoder.encode("$folderToEncode/news", "$encodeFolder/CalgaryCorpus/news.rle")
    }

    @Test
    @Order(11)
    fun decodeMixed_news() {
        mixedEncoder.decode("$encodeFolder/CalgaryCorpus/news.rle", "${decodeFolder}/CalgaryCorpus/news")
    }

    @Test
    @Order(12)
    fun encodeMixed_obj1() {
        mixedEncoder.encode("$folderToEncode/obj1", "$encodeFolder/CalgaryCorpus/obj1.rle")
    }

    @Test
    @Order(13)
    fun decodeMixed_obj1() {
        mixedEncoder.decode("$encodeFolder/CalgaryCorpus/obj1.rle", "${decodeFolder}/CalgaryCorpus/obj")
    }

    @Test
    @Order(14)
    fun encodeMixed_paper1() {
        mixedEncoder.encode("$folderToEncode/paper1", "$encodeFolder/CalgaryCorpus/paper1.rle")
    }

    @Test
    @Order(15)
    fun decodeMixed_paper1() {
        mixedEncoder.decode("$encodeFolder/CalgaryCorpus/paper1.rle", "${decodeFolder}/CalgaryCorpus/paper1")
    }

    @Test
    @Order(16)
    fun encodeMixed_paper2() {
        mixedEncoder.encode("$folderToEncode/paper2", "$encodeFolder/CalgaryCorpus/paper2.rle")
    }

    @Test
    @Order(17)
    fun decodeMixed_paper2() {
        mixedEncoder.decode("$encodeFolder/CalgaryCorpus/paper2.rle", "${decodeFolder}/CalgaryCorpus/paper2")
    }

    @Test
    @Order(18)
    fun encodeMixed_pic() {
        mixedEncoder.encode("$folderToEncode/pic", "$encodeFolder/CalgaryCorpus/pic.rle")
    }

    @Test
    @Order(19)
    fun decodeMixed_pic() {
        mixedEncoder.decode("$encodeFolder/CalgaryCorpus/pic.rle", "${decodeFolder}/CalgaryCorpus/pic")
    }

    @Test
    @Order(20)
    fun encodeMixed_progc() {
        mixedEncoder.encode("$folderToEncode/progc", "$encodeFolder/CalgaryCorpus/progc.rle")
    }

    @Test
    @Order(21)
    fun decodeMixed_progc() {
        mixedEncoder.decode("$encodeFolder/CalgaryCorpus/progc.rle", "${decodeFolder}/CalgaryCorpus/progc")
    }

    @Test
    @Order(22)
    fun encodeMixed_progl() {
        mixedEncoder.encode("$folderToEncode/progl", "$encodeFolder/CalgaryCorpus/progl.rle")
    }

    @Test
    @Order(23)
    fun decodeMixed_progl() {
        mixedEncoder.decode("$encodeFolder/CalgaryCorpus/progl.rle", "${decodeFolder}/CalgaryCorpus/progl")
    }

    @Test
    @Order(24)
    fun encodeMixed_progp() {
        mixedEncoder.encode("$folderToEncode/progp", "$encodeFolder/CalgaryCorpus/progp.rle")
    }

    @Test
    @Order(25)
    fun decodeMixed_progp() {
        mixedEncoder.decode("$encodeFolder/CalgaryCorpus/progp.rle", "${decodeFolder}/CalgaryCorpus/progp")
    }

    @Test
    @Order(26)
    fun encodeMixed_trans() {
        mixedEncoder.encode("$folderToEncode/trans", "$encodeFolder/CalgaryCorpus/trans.rle")
    }

    @Test
    @Order(27)
    fun decodeMixed_trans() {
        mixedEncoder.decode("$encodeFolder/CalgaryCorpus/trans.rle", "${decodeFolder}/CalgaryCorpus/trans")
    }
}