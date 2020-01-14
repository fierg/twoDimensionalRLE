package edu.ba.twoDimensionalRLE.mixed

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.analysis.Analyzer
import edu.ba.twoDimensionalRLE.encoder.mixed.ModifiedMixedEncoder
import edu.ba.twoDimensionalRLE.extensions.reversed
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
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
    @Order(5)
    fun encodeVertReadingRLEVaryingNrs() {
        log.info("Encoding with vertical byte reading and binary RLE.")

        val applyByteMapping = true
        val applyBurrowsWheelerTransformation = false
        val applyHuffmanEncoding = false
        val resultMap = mutableMapOf<Map<Int, Int>, Long>()


        if (applyByteMapping) log.info("Encoding with mapping as preprocessing.")
        if (applyBurrowsWheelerTransformation) log.info("Encoding with a Burrows Wheeler Transformation as preprocessing.")
        val deferred = mutableListOf<Deferred<Any>>()
        for (i in 2..2) {
            for (j in 2..2) {
                for (k in 3..3) {
                    for (l in 4..4) {
                        for (m in 4..4) {
                            for (n in 5..5) {
                                for (o in 5..7) {
                                    for (p in 8..8) {
                                        deferred.add(GlobalScope.async {
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
                                                "mixed"
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

    // @Test
    @Order(6)
    fun decode() {

        val applyByteMapping = true
        val applyBurrowsWheelerTransformation = true
        val applyHuffmanEncoding = false

        if (File("${decodeFolder}/CalgaryCorpus").exists()) {
            log.info("deleting directory: ${decodeFolder}/CalgaryCorpus")
            File("${decodeFolder}/CalgaryCorpus").deleteRecursively()
        }
        log.info("creating directory: ${decodeFolder}/CalgaryCorpus")
        File("${decodeFolder}/CalgaryCorpus").mkdirs()

        File("${encodeFolder}/CalgaryCorpus").listFiles().filter { it.extension.endsWith("mixed") }.forEach {
            try {
                log.info("Decoding $it")
                mixedEncoder.decodeInternal(
                    "${encodeFolder}/CalgaryCorpus/${it.name}",
                    "${decodeFolder}/CalgaryCorpus/${it.nameWithoutExtension}",
                    applyByteMapping,
                    applyBurrowsWheelerTransformation,
                    applyHuffmanEncoding,
                    mapOf(0 to 2, 1 to 2, 2 to 2, 3 to 2, 4 to 2, 5 to 2, 6 to 2, 7 to 2)
                )
            } catch (e: Exception) {
                log.error(e.toString(), e)
            }
        }

        Analyzer().sizeCompare(folderToEncode, "${decodeFolder}/CalgaryCorpus", "")
    }
}