package edu.ba.twoDimensionalRLE.mixed

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.mixed.ModifiedMixedEncoder
import kotlinx.coroutines.*
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
    val applyByteMapping = true
    val applyBurrowsWheelerTransformation = true
    val applyHuffmanEncoding = true

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
    @Order(5)
    fun encodeVertReadingRLEVaryingNrsA() {
        log.info("Encoding with vertical byte reading and binary RLE.")


        val resultMap = mutableMapOf<Map<Int, Int>, Long>()

        val deferred = mutableListOf<Deferred<Any>>()
        val context = newFixedThreadPoolContext(10, "co")

        for (i in 8..8) {
            val startTime = System.currentTimeMillis()
            deferred.add(CoroutineScope(context).async {
                val bitsPerNumberMapping =
                    mapOf(0 to i, 1 to i, 2 to i, 3 to i, 4 to i, 5 to i, 6 to i, 7 to i)

                if (File("${encodeFolder}/CalgaryCorpus/$i$i$i$i$i$i$i$i").exists()) {
                    log.info("deleting directory: ${encodeFolder}/CalgaryCorpus/$i$i$i$i$i$i$i$i")
                    File("${encodeFolder}/CalgaryCorpus/$i$i$i$i$i$i$i$i").deleteRecursively()
                    File("${decodeFolder}/CalgaryCorpus/$i$i$i$i$i$i$i$i").deleteRecursively()


                }
                log.info("creating directory: ${encodeFolder}/CalgaryCorpus/$i$i$i$i$i$i$i$i")
                File("${encodeFolder}/CalgaryCorpus/$i$i$i$i$i$i$i$i").mkdirs()
                File("${decodeFolder}/CalgaryCorpus/$i$i$i$i$i$i$i$i").mkdirs()

                File(folderToEncode).listFiles().forEach {
                    try {
                        mixedEncoder.encodeInternal(
                            "${folderToEncode}/${it.name}",
                            "${encodeFolder}/CalgaryCorpus/$i$i$i$i$i$i$i$i/${it.name}.mixed",
                            applyByteMapping,
                            applyBurrowsWheelerTransformation,
                            applyHuffmanEncoding,
                            bitsPerNumberMapping

                        )
                    } catch (e: Exception) {
                        log.error(e.toString(), e)
                    }
                }
                log.info("Finished encoding of corpus in ${(System.currentTimeMillis() - startTime).toDouble() / 1000.toDouble()}s.")

                val result = Analyzer().sizeCompare(
                    folderToEncode,
                    "${encodeFolder}/CalgaryCorpus/$i$i$i$i$i$i$i$i",
                    "mixed",
                    null,
                    bitsPerNumberMapping,
                    true
                )
                resultMap[bitsPerNumberMapping] = result
                return@async result
            })
        }


        runBlocking {
            return@runBlocking deferred.map { it.await() }
        }

        log.info("all combinations tried. result:")
        println(
            resultMap.toList().sortedBy
            { (_, value) -> value }.toMap()
        )
    }


    @Test
    @Order(6)
    fun decodeF() {

        val bitsPerRLERun = 8

        if (File("${decodeFolder}/CalgaryCorpus/88888888").exists()) {
            log.info("deleting directory: ${decodeFolder}/CalgaryCorpus/88888888")
            File("${decodeFolder}/CalgaryCorpus/88888888").deleteRecursively()
        }
        log.info("creating directory: ${decodeFolder}/CalgaryCorpus/88888888")
        File("${decodeFolder}/CalgaryCorpus/88888888").mkdirs()


        val startTime = System.currentTimeMillis()

        File("${encodeFolder}/CalgaryCorpus/88888888").listFiles().filter { it.extension == "mixed" }.forEach {
            try {

                mixedEncoder.decodeInternal(
                    "${encodeFolder}/CalgaryCorpus/88888888/${it.name}",
                    "${decodeFolder}/CalgaryCorpus/88888888/${it.nameWithoutExtension}",
                    applyByteMapping,
                    applyBurrowsWheelerTransformation,
                    applyHuffmanEncoding,
                    mapOf(
                        0 to bitsPerRLERun,
                        1 to bitsPerRLERun,
                        2 to bitsPerRLERun,
                        3 to bitsPerRLERun,
                        4 to bitsPerRLERun,
                        5 to bitsPerRLERun,
                        6 to bitsPerRLERun,
                        7 to bitsPerRLERun
                    )
                )


            } catch (e: Exception) {
                log.error(e.toString(), e)
            }
        }

        log.info("Finished decoding of corpus in ${(System.currentTimeMillis() - startTime).toDouble() / 1000.toDouble()}s.")

        Analyzer().sizeCompare(folderToEncode, "${decodeFolder}/CalgaryCorpus", "")
    }


    // @Test
    @Order(5)
    fun encodeVertReadingRLEVaryingNrs() {
        log.info("Encoding with vertical byte reading and binary RLE.")

        val resultMap = mutableMapOf<Map<Int, Int>, Long>()


        if (applyByteMapping) log.info("Encoding with mapping as preprocessing.")
        if (applyBurrowsWheelerTransformation) log.info("Encoding with a Burrows Wheeler Transformation as preprocessing.")
        val deferred = mutableListOf<Deferred<Any>>()
        val context = newFixedThreadPoolContext(10, "co")

        for (i in 8..8) {
            for (j in 8..8) {
                for (k in 8..8) {
                    for (l in 8..8) {
                        for (m in 8..8) {
                            for (n in 8..8) {
                                for (o in 8..8) {
                                    for (p in 8..8) {
                                        deferred.add(CoroutineScope(context).async {
                                            val bitsPerNumberMapping =
                                                mapOf(0 to i, 1 to j, 2 to k, 3 to l, 4 to m, 5 to n, 6 to o, 7 to p)

                                            if (File("${encodeFolder}/CalgaryCorpus/$i$j$k$l$m$n$o$p").exists()) {
                                                log.info("deleting directory: ${encodeFolder}/CalgaryCorpus/$i$j$k$l$m$n$o$p")
                                                File("${encodeFolder}/CalgaryCorpus/$i$j$k$l$m$n$o$p").deleteRecursively()
                                                File("${decodeFolder}/CalgaryCorpus/$i$j$k$l$m$n$o$p").deleteRecursively()


                                            }
                                            log.info("creating directory: ${encodeFolder}/CalgaryCorpus/$i$j$k$l$m$n$o$p")
                                            File("${encodeFolder}/CalgaryCorpus/$i$j$k$l$m$n$o$p").mkdirs()
                                            File("${decodeFolder}/CalgaryCorpus/$i$j$k$l$m$n$o$p").mkdirs()

                                            File(folderToEncode).listFiles().forEach {
                                                try {
                                                    mixedEncoder.encodeInternal(
                                                        "${folderToEncode}/${it.name}",
                                                        "${encodeFolder}/CalgaryCorpus/$i$j$k$l$m$n$o$p/${it.name}.mixed",
                                                        applyByteMapping,
                                                        applyBurrowsWheelerTransformation,
                                                        applyHuffmanEncoding,
                                                        bitsPerNumberMapping

                                                    )
                                                } catch (e: Exception) {
                                                    log.error(e.toString(), e)
                                                }
                                            }
                                            log.info("Finished encoding of corpus.")

                                            val result = Analyzer().sizeCompare(
                                                folderToEncode,
                                                "${encodeFolder}/CalgaryCorpus/$i$j$k$l$m$n$o$p",
                                                "mixed",
                                                null,
                                                bitsPerNumberMapping,
                                                true
                                            )
                                            resultMap[bitsPerNumberMapping] = result
                                            return@async result
                                        })
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
        runBlocking {
            return@runBlocking deferred.map { it.await() }
        }

        log.info("all combinations tried. result:")
        println(
            resultMap.toList().sortedBy
            { (_, value) -> value }.toMap()
        )
    }
}