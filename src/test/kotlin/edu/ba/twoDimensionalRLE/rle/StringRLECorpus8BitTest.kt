package edu.ba.twoDimensionalRLE.rle


import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.rle.StringRunLengthEncoder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File
import java.nio.file.Files
@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class StringRLECorpus8BitTest {

    private var log = Log.kotlinInstance()
    private val applyByteMapping = false
    private val applyBWT = false
    private val bitsPerRleNumber = 8

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    companion object {
        private const val folderToEncode = "data/corpus/CalgaryCorpus"
        private const val encodeFolder = "data/encoded/rle/8bit"
        private const val decodeFolder = "data/decoded/rle/8bit"
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
    fun encodeAndDecodeCorpus8BitRle() {

        File(folderToEncode).listFiles().forEach {
            strRLE.encodeVarLength(
                "$folderToEncode/${it.name}", "$encodeFolder/CalgaryCorpus/${it.name}.rle",
                applyByteMapping = applyByteMapping,
                applyBurrowsWheelerTransformation = applyBWT,
                bitPerRun = bitsPerRleNumber, chunkSize = 256
            )
            strRLE.decodeVarLength(
                "$encodeFolder/CalgaryCorpus/${it.name}.rle", "$decodeFolder/CalgaryCorpus/${it.name}",
                applyByteMapping = applyByteMapping,
                applyBurrowsWheelerTransformation = applyBWT,
                bitPerRun = bitsPerRleNumber
                , chunkSize = 256
            )
        }

        val analyzer = Analyzer()
        analyzer.sizeCompare(folderToEncode, "${encodeFolder}/CalgaryCorpus")
    }
}