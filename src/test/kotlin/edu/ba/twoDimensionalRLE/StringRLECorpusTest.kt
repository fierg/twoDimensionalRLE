package edu.ba.twoDimensionalRLE

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.encoder.rle.StringRunLengthEncoder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File

@ExperimentalUnsignedTypes
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class StringRLECorpusTest {

    private var log = Log.kotlinInstance()
    private val applyByteMapping = true
    private val applyBWT = true
    private val bitsPerRleNumber = 1

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
        strRLE.encodeVarLength(
            "$folderToEncode/book1", "$encodeFolder/CalgaryCorpus/book1.rle",
            applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(3)
    fun decodeStringRLE_book1() {
        strRLE.decodeVarLength(
            "$encodeFolder/CalgaryCorpus/book1.rle",
            "${decodeFolder}/CalgaryCorpus/book1",
            applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(4)
    fun encodeStringRLE_book2() {
        strRLE.encodeVarLength(
            "$folderToEncode/book2", "$encodeFolder/CalgaryCorpus/book2.rle", applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(5)
    fun decodeStringRLE_book2() {
        strRLE.decodeVarLength(
            "$encodeFolder/CalgaryCorpus/book2.rle",
            "${decodeFolder}/CalgaryCorpus/book2",
            applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(6)
    fun encodeStringRLE_bib() {
        strRLE.encodeVarLength(
            "$folderToEncode/bib", "$encodeFolder/CalgaryCorpus/bib.rle", applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(7)
    fun decodeStringRLE_bib() {
        strRLE.decodeVarLength(
            "$encodeFolder/CalgaryCorpus/bib.rle",
            "${decodeFolder}/CalgaryCorpus/bib",
            applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(8)
    fun encodeStringRLE_geo() {
        strRLE.encodeVarLength(
            "$folderToEncode/geo", "$encodeFolder/CalgaryCorpus/geo.rle", applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(9)
    fun decodeStringRLE_geo() {
        strRLE.decodeVarLength(
            "$encodeFolder/CalgaryCorpus/geo.rle",
            "${decodeFolder}/CalgaryCorpus/geo",
            applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(10)
    fun encodeStringRLE_news() {
        strRLE.encodeVarLength(
            "$folderToEncode/news", "$encodeFolder/CalgaryCorpus/news.rle", applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(11)
    fun decodeStringRLE_news() {
        strRLE.decodeVarLength(
            "$encodeFolder/CalgaryCorpus/news.rle", "${decodeFolder}/CalgaryCorpus/news",
            applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(12)
    fun encodeStringRLE_obj1() {
        strRLE.encodeVarLength(
            "$folderToEncode/obj1", "$encodeFolder/CalgaryCorpus/obj1.rle", applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(13)
    fun decodeStringRLE_obj1() {
        strRLE.decodeVarLength(
            "$encodeFolder/CalgaryCorpus/obj1.rle",
            "${decodeFolder}/CalgaryCorpus/obj",
            applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(14)
    fun encodeStringRLE_paper1() {
        strRLE.encodeVarLength(
            "$folderToEncode/paper1", "$encodeFolder/CalgaryCorpus/paper1.rle", applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(15)
    fun decodeStringRLE_paper1() {
        strRLE.decodeVarLength(
            "$encodeFolder/CalgaryCorpus/paper1.rle", "${decodeFolder}/CalgaryCorpus/paper1",
            applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(16)
    fun encodeStringRLE_paper2() {
        strRLE.encodeVarLength(
            "$folderToEncode/paper2", "$encodeFolder/CalgaryCorpus/paper2.rle", applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(17)
    fun decodeStringRLE_paper2() {
        strRLE.decodeVarLength(
            "$encodeFolder/CalgaryCorpus/paper2.rle",
            "${decodeFolder}/CalgaryCorpus/paper2",
            applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(18)
    fun encodeStringRLE_pic() {
        strRLE.encodeVarLength(
            "$folderToEncode/pic", "$encodeFolder/CalgaryCorpus/pic.rle", applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(19)
    fun decodeStringRLE_pic() {
        strRLE.decodeVarLength(
            "$encodeFolder/CalgaryCorpus/pic.rle",
            "${decodeFolder}/CalgaryCorpus/pic",
            applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(20)
    fun encodeStringRLE_progc() {
        strRLE.encodeVarLength(
            "$folderToEncode/progc", "$encodeFolder/CalgaryCorpus/progc.rle", applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(21)
    fun decodeStringRLE_progc() {
        strRLE.decodeVarLength(
            "$encodeFolder/CalgaryCorpus/progc.rle",
            "${decodeFolder}/CalgaryCorpus/progc",
            applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(22)
    fun encodeStringRLE_progl() {
        strRLE.encodeVarLength(
            "$folderToEncode/progl", "$encodeFolder/CalgaryCorpus/progl.rle", applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(23)
    fun decodeStringRLE_progl() {
        strRLE.decodeVarLength(
            "$encodeFolder/CalgaryCorpus/progl.rle",
            "${decodeFolder}/CalgaryCorpus/progl",
            applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(24)
    fun encodeStringRLE_progp() {
        strRLE.encodeVarLength(
            "$folderToEncode/progp", "$encodeFolder/CalgaryCorpus/progp.rle", applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(25)
    fun decodeStringRLE_progp() {
        strRLE.decodeVarLength(
            "$encodeFolder/CalgaryCorpus/progp.rle",
            "${decodeFolder}/CalgaryCorpus/progp",
            applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(26)
    fun encodeStringRLE_trans() {
        strRLE.encodeVarLength(
            "$folderToEncode/trans", "$encodeFolder/CalgaryCorpus/trans.rle", applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(27)
    fun decodeStringRLE_trans() {
        strRLE.decodeVarLength(
            "$encodeFolder/CalgaryCorpus/trans.rle",
            "${decodeFolder}/CalgaryCorpus/trans",
            applyByteMapping = applyByteMapping,
            applyBurrowsWheelerTransformation = applyBWT,
            bitPerRun = bitsPerRleNumber
        )
    }

    @Test
    @Order(28)
    fun size() {

      //  long size = Files.walk(path).mapToLong( p -> p.toFile().length() ).sum();

    }


}