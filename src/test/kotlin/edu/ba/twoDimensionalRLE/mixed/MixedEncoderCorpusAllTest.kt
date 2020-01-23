package edu.ba.twoDimensionalRLE.mixed

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.mixed.MixedEncoder
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import java.io.File

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class MixedEncoderCorpusAllTest {


    private var log = Log.kotlinInstance()
    private val byteArraySize = 512

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
    @Order(2)
    fun encodeAndDecodeCorpus2BitRle_bwt() {

        for (bitsPerRLENumber in 3..3) {

            val applyByteMapping = false
            val applyBWT = false


            if (File("$encodeFolder/CalgaryCorpus").exists()) {
                log.info("deleting directory: $encodeFolder/CalgaryCorpus")
                File("$encodeFolder/CalgaryCorpus").deleteRecursively()
                File("$decodeFolder/CalgaryCorpus").deleteRecursively()


            }
            log.info("creating directory: $encodeFolder/CalgaryCorpus")
            File("$encodeFolder/CalgaryCorpus").mkdirs()
            File("$decodeFolder/CalgaryCorpus").mkdirs()

            File(folderToEncode).listFiles().forEach {
                try {
                    mixedEncoder.encode(
                        "$folderToEncode/${it.name}",
                        "$encodeFolder/CalgaryCorpus/${it.name}.mixed",
                        applyByteMapping = applyByteMapping,
                        applyBurrowsWheelerTransformation = applyBWT,
                        byteArraySize = byteArraySize,
                        bitsPerRLENumber = bitsPerRLENumber
                    )
                    mixedEncoder.decode(
                        "$encodeFolder/CalgaryCorpus/${it.name}.mixed",
                        "$decodeFolder/CalgaryCorpus/${it.name}",
                        applyByteMapping = applyByteMapping,
                        applyBurrowsWheelerTransformation = applyBWT,
                        byteArraySize = byteArraySize,
                        bitsPerRLENumber = bitsPerRLENumber
                    )
                } catch (e: Exception) {
                    log.error(e.message.toString(), e)
                }
            }

            Analyzer().sizeCompare(folderToEncode, "$encodeFolder/CalgaryCorpus", "mixed")
        }
    }
}