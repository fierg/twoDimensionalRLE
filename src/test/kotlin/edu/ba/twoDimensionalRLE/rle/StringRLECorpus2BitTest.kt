package edu.ba.twoDimensionalRLE.rle


import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.encoder.rle.StringRunLengthEncoder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File
import java.nio.file.Files

@ExperimentalUnsignedTypes
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class StringRLECorpus2BitTest {

    private var log = Log.kotlinInstance()
    private val applyByteMapping = false
    private val applyBWT = false
    private val bitsPerRleNumber = 2

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
                bitPerRun = bitsPerRleNumber
            )
            strRLE.decodeVarLength(
                "$encodeFolder/CalgaryCorpus/${it.name}.rle", "$decodeFolder/CalgaryCorpus/${it.name}",
                applyByteMapping = applyByteMapping,
                applyBurrowsWheelerTransformation = applyBWT,
                bitPerRun = bitsPerRleNumber
            )
        }

    }

    @Test
    @Order(3)
    fun size() {
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