package edu.ba.twoDimensionalRLE

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.encoder.rle.StringRunLengthEncoder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class StringRLECorpusTest {

    private var log = Log.kotlinInstance()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    companion object {
        private const val folderToEncode = "data/corpus/CalgaryCorpus"
        private const val encodeFolder = "data/encoded/rle"
        private const val decodeFolder = "data/decoded/rle"
    }

    private val strRLE = StringRunLengthEncoder()

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
    fun encodeStringRLE_book1() {
        strRLE.encode(
            "$folderToEncode/book1", "$encodeFolder/CalgaryCorpus/book1.rle",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(3)
    fun decodeStringRLE_book1() {
        strRLE.decode(
            "$encodeFolder/CalgaryCorpus/book1.rle", "${decodeFolder}/CalgaryCorpus/book1",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(4)
    fun encodeStringRLE_book2() {
        strRLE.encode(
            "$folderToEncode/book2", "$encodeFolder/CalgaryCorpus/book2.rle",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(5)
    fun decodeStringRLE_book2() {
        strRLE.decode(
            "$encodeFolder/CalgaryCorpus/book2.rle", "${decodeFolder}/CalgaryCorpus/book2",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(6)
    fun encodeStringRLE_bib() {
        strRLE.encode(
            "$folderToEncode/bib", "$encodeFolder/CalgaryCorpus/bib.rle",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(7)
    fun decodeStringRLE_bib() {
        strRLE.decode(
            "$encodeFolder/CalgaryCorpus/bib.rle", "${decodeFolder}/CalgaryCorpus/bib",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(8)
    fun encodeStringRLE_geo() {
        strRLE.encode(
            "$folderToEncode/geo", "$encodeFolder/CalgaryCorpus/geo.rle",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(9)
    fun decodeStringRLE_geo() {
        strRLE.decode(
            "$encodeFolder/CalgaryCorpus/geo.rle", "${decodeFolder}/CalgaryCorpus/geo",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(10)
    fun encodeStringRLE_news() {
        strRLE.encode(
            "$folderToEncode/news", "$encodeFolder/CalgaryCorpus/news.rle",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(11)
    fun decodeStringRLE_news() {
        strRLE.decode(
            "$encodeFolder/CalgaryCorpus/news.rle", "${decodeFolder}/CalgaryCorpus/news",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(12)
    fun encodeStringRLE_obj1() {
        strRLE.encode(
            "$folderToEncode/obj1", "$encodeFolder/CalgaryCorpus/obj1.rle",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(13)
    fun decodeStringRLE_obj1() {
        strRLE.decode(
            "$encodeFolder/CalgaryCorpus/obj1.rle", "${decodeFolder}/CalgaryCorpus/obj",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(14)
    fun encodeStringRLE_paper1() {
        strRLE.encode(
            "$folderToEncode/paper1", "$encodeFolder/CalgaryCorpus/paper1.rle",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(15)
    fun decodeStringRLE_paper1() {
        strRLE.decode(
            "$encodeFolder/CalgaryCorpus/paper1.rle", "${decodeFolder}/CalgaryCorpus/paper1",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(16)
    fun encodeStringRLE_paper2() {
        strRLE.encode(
            "$folderToEncode/paper2", "$encodeFolder/CalgaryCorpus/paper2.rle",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(17)
    fun decodeStringRLE_paper2() {
        strRLE.decode(
            "$encodeFolder/CalgaryCorpus/paper2.rle", "${decodeFolder}/CalgaryCorpus/paper2",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(18)
    fun encodeStringRLE_pic() {
        strRLE.encode(
            "$folderToEncode/pic", "$encodeFolder/CalgaryCorpus/pic.rle",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(19)
    fun decodeStringRLE_pic() {
        strRLE.decode(
            "$encodeFolder/CalgaryCorpus/pic.rle", "${decodeFolder}/CalgaryCorpus/pic",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(20)
    fun encodeStringRLE_progc() {
        strRLE.encode(
            "$folderToEncode/progc", "$encodeFolder/CalgaryCorpus/progc.rle",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(21)
    fun decodeStringRLE_progc() {
        strRLE.decode(
            "$encodeFolder/CalgaryCorpus/progc.rle", "${decodeFolder}/CalgaryCorpus/progc",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(22)
    fun encodeStringRLE_progl() {
        strRLE.encode(
            "$folderToEncode/progl", "$encodeFolder/CalgaryCorpus/progl.rle",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(23)
    fun decodeStringRLE_progl() {
        strRLE.decode(
            "$encodeFolder/CalgaryCorpus/progl.rle", "${decodeFolder}/CalgaryCorpus/progl",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(24)
    fun encodeStringRLE_progp() {
        strRLE.encode(
            "$folderToEncode/progp", "$encodeFolder/CalgaryCorpus/progp.rle",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(25)
    fun decodeStringRLE_progp() {
        strRLE.decode(
            "$encodeFolder/CalgaryCorpus/progp.rle", "${decodeFolder}/CalgaryCorpus/progp",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(26)
    fun encodeStringRLE_trans() {
        strRLE.encode(
            "$folderToEncode/trans", "$encodeFolder/CalgaryCorpus/trans.rle",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }

    @Test
    @Order(27)
    fun decodeStringRLE_trans() {
        strRLE.decode(
            "$encodeFolder/CalgaryCorpus/trans.rle", "${decodeFolder}/CalgaryCorpus/trans",
            applyByteMapping = true,
            applyBurrowsWheelerTransformation = true
        )
    }


}