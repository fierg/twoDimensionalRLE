package edu.ba.twoDimensionalRLE.rle


import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.rle.StringRunLengthEncoder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ByteRLECorpusTest {

    private var log = Log.kotlinInstance()
    private val applyByteMapping = false
    private val applyBWT = false
    private val applyBWTS = true

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    companion object {
        private const val folderToEncode = "data/corpus/CalgaryCorpus"
        private const val encodeFolder = "data/encoded/rle/2bit"
        private const val decodeFolder = "data/decoded/rle/2bit"
    }

    private val strRLE = StringRunLengthEncoder()

    @Test
    @Order(2)
    fun encodeAndDecodeCorpus() {
        for (i in 4..4) {
            if (File("$encodeFolder/CalgaryCorpus").exists()) {
                log.info("deleting directory: $encodeFolder/CalgaryCorpus")
                File("$encodeFolder/CalgaryCorpus").deleteRecursively()
                File("$decodeFolder/CalgaryCorpus").deleteRecursively()


            }
            log.info("creating directory: $encodeFolder/CalgaryCorpus")
            File("$encodeFolder/CalgaryCorpus").mkdirs()
            File("$decodeFolder/CalgaryCorpus").mkdirs()


            File(folderToEncode).listFiles().forEach {
                strRLE.encodeVarLength(
                    "$folderToEncode/${it.name}", "$encodeFolder/CalgaryCorpus/${it.name}.rle",
                    applyByteMapping = applyByteMapping,
                    applyBurrowsWheelerTransformationS = applyBWTS,
                    applyBurrowsWheelerTransformation = applyBWT,
                    bitPerRun = i, chunkSize = 256
                )
                strRLE.decodeVarLength(
                    "$encodeFolder/CalgaryCorpus/${it.name}.rle", "$decodeFolder/CalgaryCorpus/${it.name}",
                    applyByteMapping = applyByteMapping,
                    applyBurrowsWheelerTransformation = applyBWT,
                    applyBurrowsWheelerTransformationS = applyBWTS,
                    bitPerRun = i, chunkSize = 256
                )
            }

            val analyzer = Analyzer()
            analyzer.sizeCompare(folderToEncode, "${encodeFolder}/CalgaryCorpus", null, null, null, true)
        }
    }
}