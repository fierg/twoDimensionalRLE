package edu.ba.twoDimensionalRLE.mixed

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.encoder.mixed.MixedEncoder
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

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

        val applyByteMapping = false
        val applyBWT = true
        val bitsPerRleNumber = 2


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
                    "$folderToEncode/${it.name}", "$encodeFolder/CalgaryCorpus/${it.name}.mixed",
                    applyByteMapping = applyByteMapping,
                    applyBurrowsWheelerTransformation = applyBWT, byteArraySize = byteArraySize
                )
                mixedEncoder.decode(
                    "$encodeFolder/CalgaryCorpus/${it.name}.mixed", "$decodeFolder/CalgaryCorpus/${it.name}",
                    applyByteMapping = applyByteMapping,
                    applyBurrowsWheelerTransformation = applyBWT, byteArraySize = byteArraySize
                )
            } catch (e: Exception) {
                log.warn(e.localizedMessage)
            }
        }

        val sizeOriginal = Files.walk(File(folderToEncode).toPath()).map { mapper -> mapper.toFile().length() }
            .reduce { t: Long, u: Long -> t + u }.get()
        val sizeEncoded =
            Files.walk(File("$encodeFolder/CalgaryCorpus").toPath()).map { mapper -> mapper.toFile().length() }
                .reduce { t: Long, u: Long -> t + u }.get()
        val bitsPerSymbol = (sizeEncoded * 8).toDouble() / sizeOriginal.toDouble()

        log.info("Galgary Corpus size original: ${sizeOriginal / 1000000.0} Mb")
        log.info("Galgary Corpus size encoded: ${sizeEncoded / 1000000.0} Mb")

        log.info("${sizeEncoded.toDouble() / sizeOriginal.toDouble()} compression ratio")
        log.info("with $bitsPerSymbol bits/symbol")

    }
}