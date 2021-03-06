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
    val applyHuffmanEncoding = false

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

    //@Test
    @Order(5)
    fun encodeVertReadingRLEVaryingNrsA() {
        log.info("Encoding with vertical byte reading and binary RLE.")


        val resultMap = mutableMapOf<Map<Int, Int>, Long>()

        val deferred = mutableListOf<Deferred<Any>>()
        val context = newFixedThreadPoolContext(12, "co")

        for (i in 8..8) {
            val startTime = System.currentTimeMillis()
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

            File(folderToEncode).listFiles().forEachIndexed { index, file ->
                deferred.add(CoroutineScope(context).async {

                    try {
                        mixedEncoder.encodeInternal(
                            "${folderToEncode}/${file.name}",
                            "${encodeFolder}/CalgaryCorpus/$i$i$i$i$i$i$i$i/${file.name}.mixed",
                            applyByteMapping,
                            applyBurrowsWheelerTransformation,
                            applyHuffmanEncoding,
                            bitsPerNumberMapping

                        )
                    } catch (e: Exception) {
                        log.error(e.toString(), e)
                    }
                    return@async index
                })
            }
            runBlocking {
                return@runBlocking deferred.map { it.await() }
            }

            log.info("Finished encoding of corpus in ${(System.currentTimeMillis() - startTime).toDouble() / 1000.toDouble()}s.")
            Analyzer().sizeCompare(
                folderToEncode,
                "${encodeFolder}/CalgaryCorpus/$i$i$i$i$i$i$i$i",
                "mixed",
                null,
                bitsPerNumberMapping,
                true
            )


        }




        log.info("all combinations tried. result:")
        println(
            resultMap.toList().sortedBy
            { (_, value) -> value }.toMap()
        )
    }

    @Test
    @Order(5)
    fun encodeVertReadingRLEVaryingNrs1() {
        log.info("Encoding with vertical byte reading and binary RLE.")

        val resultMap = mutableMapOf<Map<Int, Int>, Long>()

        val mapset = mutableSetOf<Map<Int, Int>>()

        mapset.add(mapOf(0 to 2, 1 to 2, 2 to 2, 3 to 2, 4 to 3, 5 to 4, 6 to 3, 7 to 6))
        mapset.add(mapOf(0 to 2, 1 to 2, 2 to 2, 3 to 2, 4 to 3, 5 to 4, 6 to 3, 7 to 7))

        mapset.add(mapOf(0 to 2, 1 to 2, 2 to 3, 3 to 3, 4 to 3, 5 to 4, 6 to 5, 7 to 8))
        mapset.add(mapOf(0 to 2, 1 to 2, 2 to 2, 3 to 3, 4 to 3, 5 to 5, 6 to 7, 7 to 9))

        mapset.add(mapOf(0 to 4, 1 to 4, 2 to 4, 3 to 4, 4 to 4, 5 to 6, 6 to 6, 7 to 8))
        mapset.add(mapOf(0 to 4, 1 to 4, 2 to 4, 3 to 4, 4 to 4, 5 to 6, 6 to 7, 7 to 9))

        mapset.add(mapOf(0 to 4, 1 to 4, 2 to 4, 3 to 4, 4 to 5, 5 to 7, 6 to 8, 7 to 8))
        mapset.add(mapOf(0 to 4, 1 to 4, 2 to 4, 3 to 4, 4 to 5, 5 to 7, 6 to 8, 7 to 10))



        if (applyByteMapping) log.info("Encoding with mapping as preprocessing.")
        if (applyBurrowsWheelerTransformation) log.info("Encoding with a Burrows Wheeler Transformation as preprocessing.")
        val deferred = mutableListOf<Deferred<Any>>()
        val context = newFixedThreadPoolContext(10, "co")

        for (i in 2..4) {
            for (j in 2..4) {
                for (k in 2..4) {
                    for (l in 2..4) {
                        for (m in 2..5) {
                            for (n in 2..7) {
                                for (o in 2..8) {
                                    for (p in 2..10) {

                                        deferred.add(CoroutineScope(context).async {

                                            val bitsPerNumberMapping =
                                                mapOf(0 to i, 1 to j, 2 to k, 3 to l, 4 to m, 5 to n, 6 to o, 7 to p)

                                            if (mapset.contains(bitsPerNumberMapping)) {

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
                                                return@async
                                            } else {
                                                return@async
                                            }
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
        val deferred = mutableListOf<Deferred<Any>>()
        val context = newFixedThreadPoolContext(12, "co")

        File("${encodeFolder}/CalgaryCorpus/88888888").listFiles().filter { it.extension == "mixed" }
            .forEachIndexed { index, file ->
                deferred.add(CoroutineScope(context).async {

                    try {

                        mixedEncoder.decodeInternal(
                            "${encodeFolder}/CalgaryCorpus/88888888/${file.name}",
                            "${decodeFolder}/CalgaryCorpus/88888888/${file.nameWithoutExtension}",
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
                    return@async index
                })
            }

        runBlocking {
            return@runBlocking deferred.map { it.await() }
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
        val context = newFixedThreadPoolContext(12, "co")

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