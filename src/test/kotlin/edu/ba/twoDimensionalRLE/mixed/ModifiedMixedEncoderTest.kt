package edu.ba.twoDimensionalRLE.mixed

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.mixed.ModifiedMixedEncoder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
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

    private val mixedEncoder = ModifiedMixedEncoder()

    @Test
    @Order(1)
    fun encodeVertReadingRLEallNrEqual() {

        log.info("Encoding with vertical byte reading and binary RLE, no transformations, all bits per number are the same.")

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
                    mixedEncoder.encodeInternal(
                        "${folderToEncode}/${it.name}",
                        "${encodeFolder}/CalgaryCorpus/${it.name}.mixed",
                        bitsPerRLENumber1 = bitsPerRleNumber,
                        bitsPerRLENumber2 = bitsPerRleNumber,
                        applyByteMapping = false,
                        applyBurrowsWheelerTransformation = false,
                        splitPosition = 6
                    )
                } catch (e: Exception) {
                    log.error(e.toString(), e)
                }
            }

            log.info("Finished encoding of corpus.")

            Analyzer().sizeCompare(folderToEncode, "${encodeFolder}/CalgaryCorpus", "mixed")
        }
    }

    @Test
    @Order(2)
    fun encodeVertReadingRLEallNrEqualBWTS() {

        log.info("Encoding with vertical byte reading and binary RLE, with inverse BWT, all bits per number are the same.")

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
                    mixedEncoder.encodeInternal(
                        "${folderToEncode}/${it.name}",
                        "${encodeFolder}/CalgaryCorpus/${it.name}.mixed",
                        bitsPerRLENumber1 = bitsPerRleNumber,
                        bitsPerRLENumber2 = bitsPerRleNumber,
                        applyByteMapping = false,
                        applyBurrowsWheelerTransformation = true,
                        splitPosition = 6
                    )
                } catch (e: Exception) {
                    log.error(e.toString(), e)
                }
            }

            log.info("Finished encoding of corpus.")

            Analyzer().sizeCompare(folderToEncode, "${encodeFolder}/CalgaryCorpus", "mixed")
        }
    }

    @Test
    @Order(3)
    fun encodeVertReadingRLEallNrEqualMapping() {

        log.info("Encoding with vertical byte reading and binary RLE, with byte mapping, all bits per number are the same.")

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
                    mixedEncoder.encodeInternal(
                        "${folderToEncode}/${it.name}",
                        "${encodeFolder}/CalgaryCorpus/${it.name}.mixed",
                        bitsPerRLENumber1 = bitsPerRleNumber,
                        bitsPerRLENumber2 = bitsPerRleNumber,
                        applyByteMapping = true,
                        applyBurrowsWheelerTransformation = false,
                        splitPosition = 6
                    )
                } catch (e: Exception) {
                    log.error(e.toString(), e)
                }
            }

            log.info("Finished encoding of corpus.")

            Analyzer().sizeCompare(folderToEncode, "${encodeFolder}/CalgaryCorpus", "mixed")
        }
    }
    @Test
    @Order(4)
    fun encodeVertReadingRLEallNrEqualMappingAndBWT() {

        log.info("Encoding with vertical byte reading and binary RLE, with byte mapping and inverse BWT, all bits per number are the same.")

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
                    mixedEncoder.encodeInternal(
                        "${folderToEncode}/${it.name}",
                        "${encodeFolder}/CalgaryCorpus/${it.name}.mixed",
                        bitsPerRLENumber1 = bitsPerRleNumber,
                        bitsPerRLENumber2 = bitsPerRleNumber,
                        applyByteMapping = true,
                        applyBurrowsWheelerTransformation = true,
                        splitPosition = 6
                    )
                } catch (e: Exception) {
                    log.error(e.toString(), e)
                }
            }

            log.info("Finished encoding of corpus.")

            Analyzer().sizeCompare(folderToEncode, "${encodeFolder}/CalgaryCorpus", "mixed")
        }
    }


    @Test
    @Order(5)
    fun encodeVertReadingRLEVaryingNrs() {

        log.info("Encoding with vertical byte reading and binary RLE, with byte remapping, using more bits per rle nr for higher order bits.")

        for (splitPosition in 4..6) {
            for (bitsPerRleNumber in 2..4) {
                for (bitsPerRleNumber2 in 3..8) {
                    if (bitsPerRleNumber2 != bitsPerRleNumber) {
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
                                log.info("Encoding ${it.name} with $bitsPerRleNumber bits per RLE number for the lower significance bits and $bitsPerRleNumber2 for the highest ${7 - splitPosition} bits...")
                                mixedEncoder.encodeInternal(
                                    "${folderToEncode}/${it.name}",
                                    "${encodeFolder}/CalgaryCorpus/${it.name}.mixed",
                                    bitsPerRLENumber1 = bitsPerRleNumber2,
                                    bitsPerRLENumber2 = bitsPerRleNumber,
                                    applyByteMapping = true, splitPosition = splitPosition,
                                    applyBurrowsWheelerTransformation = true
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
        }
    }

}