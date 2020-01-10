package edu.ba.twoDimensionalRLE.mixed

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.mixed.ModfiedMixedEncoder
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import java.io.File

@ExperimentalUnsignedTypes
class ModifiedMixedEncoderTest {


    private var log = Log.kotlinInstance()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

    companion object {
        private const val folderToEncode = "data/corpus/CalgaryCorpus"
        private const val encodeFolder = "data/encoded/mod_mixed"
        private const val decodeFolder = "data/decoded/mod_mixed"
    }

    private val mixedEncoder = ModfiedMixedEncoder()

    @Test
    @Order(2)
    fun encodeFileSmall() {

        log.info("Encoding with vertical byte reading and binary RLE, no transformations or preprocessing.")

        for (bitsPerRleNumber in 2..8) {
            if (File("${encodeFolder}/CalgaryCorpus").exists()) {
                log.info("deleting directory: ${encodeFolder}/CalgaryCorpus")
                File("${encodeFolder}/CalgaryCorpus").deleteRecursively()
                File("${decodeFolder}/CalgaryCorpus").deleteRecursively()


            }
            log.info("creating directory: ${encodeFolder}/CalgaryCorpus")
            File("${encodeFolder}/CalgaryCorpus").mkdirs()
            File("${decodeFolder}/CalgaryCorpus").mkdirs()

            File(folderToEncode).listFiles().forEach {
                try {
                    log.info("Encoding ${it.name} with $bitsPerRleNumber bits per RLE number...")
                    mixedEncoder.encode(
                        "${folderToEncode}/${it.name}",
                        "${encodeFolder}/CalgaryCorpus/${it.name}.mixed",
                        bitsPerRLENumber = bitsPerRleNumber
                    )
                } catch (e: Exception) {
                    log.error(e.toString(), e)
                }
            }

            log.info("Finished encoding of corpus.")
            
            Analyzer().sizeCompare(folderToEncode, "${encodeFolder}/CalgaryCorpus", "mixed")
        }


    }

}