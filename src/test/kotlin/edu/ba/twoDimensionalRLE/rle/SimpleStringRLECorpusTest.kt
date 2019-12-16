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

@ExperimentalUnsignedTypes
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class SimpleStringRLECorpusTest {

    private var log = Log.kotlinInstance()
    private val strRLE = StringRunLengthEncoder()
    private val analyzer = Analyzer()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    companion object {
        private const val folderToEncode = "data/corpus/CalgaryCorpus"
        private const val encodeFolder = "data/encoded/simple_rle"
        private const val decodeFolder = "data/decoded/simple_rle"
    }


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
    fun encodeSimpleRLE() {
        for (i in 2..8) {
            File("$encodeFolder/CalgaryCorpus/${i}bit").mkdirs()
            File(folderToEncode).listFiles().forEach {
                strRLE.encodeSimple(
                    "${folderToEncode}/${it.name}",
                    "${encodeFolder}/CalgaryCorpus/${i}bit/${it.name}.rle",
                    i
                )
            }
            analyzer.sizeCompare(folderToEncode, "${encodeFolder}/CalgaryCorpus/${i}bit")
        }

    }
}