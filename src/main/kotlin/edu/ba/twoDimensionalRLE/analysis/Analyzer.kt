package edu.ba.twoDimensionalRLE.analysis

import de.jupf.staticlog.Log
import de.jupf.staticlog.core.LogLevel
import java.io.File
import java.nio.file.Files

class Analyzer() {
    private var log = Log.kotlinInstance()
    private val encodingOccurrenceMap = mutableMapOf<Int, Int>()
    private val byteOccurrenceMap = mutableMapOf<Byte, Int>()
    private var byteMapping = mutableMapOf<Byte, Byte>()
    private val DEBUG = true

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
        if (!DEBUG) log.logLevel = LogLevel.INFO
    }

    fun printMappingToFile(outputFile: File) {
        val result = mutableListOf<Byte>()
        byteMapping.mapKeys { entry: Map.Entry<Byte, Byte> ->
            result.add(entry.key)
        }
        outputFile.outputStream().write(result.toByteArray())
    }

    fun addBWTSymbolsToMapping() {
        val currentMax = byteMapping.values.max()

        byteMapping[2.toByte()] = currentMax!!.inc()
        byteMapping[3.toByte()] = currentMax.inc().inc()
    }

    fun printFileComparison(source: File, target: File) {
        log.info("Encoded File has a file size of ${target.length() / 1000000.toDouble()} MB")
        log.info("${(target.length() / source.length().toDouble())} times the original size")
    }

    fun printOccurrenceMap() {
        log.debug("Repetition counts: ")
        encodingOccurrenceMap.keys.distinct().sorted().forEach {
            log.info("$it ${encodingOccurrenceMap[it]}")
        }
    }

    fun printByteOccurrence() {
        byteOccurrenceMap.values.distinct().sortedDescending().forEach {
            byteOccurrenceMap.filter { entry -> entry.value == it }.toSortedMap().forEach { (byte, int) ->
                log.debug("byte $byte occurred $int times in the original stream")
            }
        }
    }

    private fun createByteMapping(): MutableMap<Byte, Byte> {
        val resultMap = mutableMapOf<Byte, Byte>()
        var counter = 0

        byteOccurrenceMap.values.distinct().sortedDescending().forEach { key ->
            byteOccurrenceMap.filter { it.value == key }.keys.sorted().forEach { byte ->
                log.debug("mapping entry: <${byte} -- ${counter.toByte()}>")
                resultMap[byte] = counter++.toByte()
            }
        }
        return resultMap
    }

    fun getByteMapping(): MutableMap<Byte, Byte> {
        if (byteMapping.isNullOrEmpty()) {
            byteMapping = createByteMapping()
        }
        return byteMapping
    }

    fun analyzeFile(file: File) {

        file.inputStream().readBytes().forEach { byte ->
            byteOccurrenceMap[byte] = byteOccurrenceMap.getOrDefault(byte, 0) + 1
        }
        printByteOccurrence()
        byteMapping = createByteMapping()
    }

    fun incrementEncodingOccMap(counter: Int) {
        encodingOccurrenceMap[counter] = encodingOccurrenceMap.getOrDefault(counter, 0) + 1

    }

    fun sizeCompare(folderToEncode : String, encodedFolder : String) {
        val sizeOriginal = Files.walk(File(folderToEncode).toPath()).map { mapper -> mapper.toFile().length() }
            .reduce { t: Long, u: Long -> t + u }.get()
        val sizeEncoded =
            Files.walk(File(encodedFolder).toPath()).map { mapper -> mapper.toFile().length() }
                .reduce { t: Long, u: Long -> t + u }.get()
        val bitsPerSymbol = (sizeEncoded * 8).toDouble() / sizeOriginal.toDouble()

        log.info("Corpus size original: ${sizeOriginal / 1000000.0} Mb")
        log.info("Corpus size encoded: ${sizeEncoded / 1000000.0} Mb")

        log.info("${sizeEncoded.toDouble() / sizeOriginal.toDouble()} compression ratio")
        log.info("with $bitsPerSymbol bits/symbol")
    }

}