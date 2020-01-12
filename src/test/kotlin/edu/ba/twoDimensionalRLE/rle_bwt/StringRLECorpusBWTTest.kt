package edu.ba.twoDimensionalRLE.rle_bwt


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
class StringRLECorpusBWTTest {

    private var log = Log.kotlinInstance()
    private val strRLE = StringRunLengthEncoder()
    private val chunksize = 512


    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    @Test
    @Order(2)
    fun encodeAndDecodeCorpusNBitRle_bwt() {

        val applyByteMapping = false
        val applyBWT = false
        val applyBWTS = true

        val folderToEncode = "data/corpus/CalgaryCorpus"
        val encodeFolder = "data/encoded/rle_bwt"
        val decodeFolder = "data/decoded/rle_bwt"

        for (i in 2..8) {
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
                    applyBurrowsWheelerTransformation = applyBWT,
                    applyBurrowsWheelerTransformationS = applyBWTS,
                    bitPerRun = i, chunkSize = chunksize
                )
                strRLE.decodeVarLength(
                    "$encodeFolder/CalgaryCorpus/${it.name}.rle", "$decodeFolder/CalgaryCorpus/${it.name}",
                    applyByteMapping = applyByteMapping,
                    applyBurrowsWheelerTransformation = applyBWT,
                    applyBurrowsWheelerTransformationS = applyBWTS,
                    bitPerRun = i
                    , chunkSize = chunksize
                )
            }
            Analyzer().sizeCompare(folderToEncode, encodeFolder)
        }
    }
}