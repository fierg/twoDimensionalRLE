package edu.ba.twoDimensionalRLE.rle_bwt


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
    fun encodeAndDecodeCorpus2BitRle_bwt() {

        val applyByteMapping = false
        val applyBWT = true
        val bitsPerRleNumber = 2

        val folderToEncode = "data/corpus/CalgaryCorpus"
        val encodeFolder = "data/encoded/rle_bwt/2bit"
        val decodeFolder = "data/decoded/rle_bwt/2bit"

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
                bitPerRun = bitsPerRleNumber, chunkSize = chunksize
            )
            strRLE.decodeVarLength(
                "$encodeFolder/CalgaryCorpus/${it.name}.rle", "$decodeFolder/CalgaryCorpus/${it.name}",
                applyByteMapping = applyByteMapping,
                applyBurrowsWheelerTransformation = applyBWT,
                bitPerRun = bitsPerRleNumber
                , chunkSize = chunksize
            )
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

    @Test
    @Order(3)
    fun encodeAndDecodeCorpus3BitRle_bwt() {

        val applyByteMapping = false
        val applyBWT = true
        val bitsPerRleNumber = 3

        val folderToEncode = "data/corpus/CalgaryCorpus"
        val encodeFolder = "data/encoded/rle_bwt/3bit"
        val decodeFolder = "data/decoded/rle_bwt/3bit"

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
                bitPerRun = bitsPerRleNumber, chunkSize = chunksize
            )
            strRLE.decodeVarLength(
                "$encodeFolder/CalgaryCorpus/${it.name}.rle", "$decodeFolder/CalgaryCorpus/${it.name}",
                applyByteMapping = applyByteMapping,
                applyBurrowsWheelerTransformation = applyBWT,
                bitPerRun = bitsPerRleNumber
                , chunkSize = chunksize
            )
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

    @Test
    @Order(4)
    fun encodeAndDecodeCorpus4BitRle_bwt() {

        val applyByteMapping = false
        val applyBWT = true
        val bitsPerRleNumber = 4

        val folderToEncode = "data/corpus/CalgaryCorpus"
        val encodeFolder = "data/encoded/rle_bwt/3bit"
        val decodeFolder = "data/decoded/rle_bwt/3bit"

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
                bitPerRun = bitsPerRleNumber, chunkSize = chunksize
            )
            strRLE.decodeVarLength(
                "$encodeFolder/CalgaryCorpus/${it.name}.rle", "$decodeFolder/CalgaryCorpus/${it.name}",
                applyByteMapping = applyByteMapping,
                applyBurrowsWheelerTransformation = applyBWT,
                bitPerRun = bitsPerRleNumber
                , chunkSize = chunksize
            )
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

    @Test
    @Order(5)
    fun encodeAndDecodeCorpus5BitRle_bwt() {

        val applyByteMapping = false
        val applyBWT = true
        val bitsPerRleNumber = 5

        val folderToEncode = "data/corpus/CalgaryCorpus"
        val encodeFolder = "data/encoded/rle_bwt/5bit"
        val decodeFolder = "data/decoded/rle_bwt/5bit"

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
                bitPerRun = bitsPerRleNumber, chunkSize = chunksize
            )
            strRLE.decodeVarLength(
                "$encodeFolder/CalgaryCorpus/${it.name}.rle", "$decodeFolder/CalgaryCorpus/${it.name}",
                applyByteMapping = applyByteMapping,
                applyBurrowsWheelerTransformation = applyBWT,
                bitPerRun = bitsPerRleNumber
                , chunkSize = chunksize
            )
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

    @Test
    @Order(6)
    fun encodeAndDecodeCorpus6BitRle_bwt() {

        val applyByteMapping = false
        val applyBWT = true
        val bitsPerRleNumber = 6

        val folderToEncode = "data/corpus/CalgaryCorpus"
        val encodeFolder = "data/encoded/rle_bwt/6bit"
        val decodeFolder = "data/decoded/rle_bwt/6bit"

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
                bitPerRun = bitsPerRleNumber, chunkSize = chunksize
            )
            strRLE.decodeVarLength(
                "$encodeFolder/CalgaryCorpus/${it.name}.rle", "$decodeFolder/CalgaryCorpus/${it.name}",
                applyByteMapping = applyByteMapping,
                applyBurrowsWheelerTransformation = applyBWT,
                bitPerRun = bitsPerRleNumber
                , chunkSize = chunksize
            )
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

    @Test
    @Order(7)
    fun encodeAndDecodeCorpus7BitRle_bwt() {

        val applyByteMapping = false
        val applyBWT = true
        val bitsPerRleNumber = 7

        val folderToEncode = "data/corpus/CalgaryCorpus"
        val encodeFolder = "data/encoded/rle_bwt/7bit"
        val decodeFolder = "data/decoded/rle_bwt/7bit"

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
                bitPerRun = bitsPerRleNumber, chunkSize = chunksize
            )
            strRLE.decodeVarLength(
                "$encodeFolder/CalgaryCorpus/${it.name}.rle", "$decodeFolder/CalgaryCorpus/${it.name}",
                applyByteMapping = applyByteMapping,
                applyBurrowsWheelerTransformation = applyBWT,
                bitPerRun = bitsPerRleNumber, chunkSize = chunksize
            )
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

    @Test
    @Order(8)
    fun encodeAndDecodeCorpus8BitRle_bwt() {

        val applyByteMapping = false
        val applyBWT = true
        val bitsPerRleNumber = 8

        val folderToEncode = "data/corpus/CalgaryCorpus"
        val encodeFolder = "data/encoded/rle_bwt/8bit"
        val decodeFolder = "data/decoded/rle_bwt/8bit"

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
                bitPerRun = bitsPerRleNumber, chunkSize =chunksize
            )
            strRLE.decodeVarLength(
                "$encodeFolder/CalgaryCorpus/${it.name}.rle", "$decodeFolder/CalgaryCorpus/${it.name}",
                applyByteMapping = applyByteMapping,
                applyBurrowsWheelerTransformation = applyBWT,
                bitPerRun = bitsPerRleNumber
                , chunkSize = chunksize
            )
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