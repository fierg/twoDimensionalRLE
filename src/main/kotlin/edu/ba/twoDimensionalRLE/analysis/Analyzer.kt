package edu.ba.twoDimensionalRLE.analysis

import de.jupf.staticlog.Log
import de.jupf.staticlog.core.LogLevel
import loggersoft.kotlin.streams.BitStream
import loggersoft.kotlin.streams.openBinaryStream
import java.io.File
import java.nio.file.Files

@ExperimentalUnsignedTypes
class Analyzer {
    private var log = Log.kotlinInstance()
    private val encodingOccurrenceMap = mutableMapOf<Int, Int>()
    private val byteOccurrenceMap = mutableMapOf<Byte, Int>()
    private var byteMapping = mutableMapOf<Byte, Byte>()
    private val DEBUG = false

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

    fun analyzeString(string: String) {

        string.forEach { byte ->
            byteOccurrenceMap[byte.toByte()] = byteOccurrenceMap.getOrDefault(byte.toByte(), 0) + 1
        }
        printByteOccurrence()
        byteMapping = createByteMapping()
    }


    fun incrementEncodingOccMap(counter: Int) {
        encodingOccurrenceMap[counter] = encodingOccurrenceMap.getOrDefault(counter, 0) + 1

    }

    fun sizeCompare(folderToEncode: String, encodedFolder: String) {
        sizeCompare(folderToEncode, encodedFolder, null, null)
    }

    fun sizeCompare(folderToEncode: String, encodedFolder: String, filterExtension: String) {
        sizeCompare(folderToEncode, encodedFolder, filterExtension, null)
    }

    fun sizeCompare(folderToEncode: String, encodedFolder: String, filterExtension: String?, filterFile: String?) {
        val originalFiles = mutableMapOf<File, Long>()
        Files.walk(File(folderToEncode).toPath()).sorted().map { mapper -> mapper.toFile() to mapper.toFile().length() }
            .forEach {
                originalFiles[it.first] = it.second
            }
        val sizeOriginal = originalFiles.map { it.value }.reduce { l: Long?, l2: Long? -> l!! + l2!! }

        val encodedFiles = mutableMapOf<File, Long>()
        Files.walk(File(encodedFolder).toPath()).map { mapper -> mapper.toFile() to mapper.toFile().length() }
            .filter { !it.first.name.endsWith("_tmp") }.forEach {
            encodedFiles[it.first] = it.second
        }


        val sizeEncoded: Long
        if (filterExtension.isNullOrEmpty()) {
            sizeEncoded = encodedFiles.map { it.value }.reduce { t: Long, u: Long -> t + u }
        } else {
            sizeEncoded = encodedFiles.map { it.value }.reduce { t: Long, u: Long -> t + u }
        }


        val bitsPerSymbol = (sizeEncoded * 8).toDouble() / sizeOriginal.toDouble()

        log.info("\n\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n")
        log.info("Corpus size original: $sizeOriginal // ${sizeOriginal / 1000000.0} Mb")
        log.info("Corpus size encoded: $sizeEncoded // ${sizeEncoded / 1000000.0} Mb")

        log.info("${sizeEncoded.toDouble() / sizeOriginal.toDouble()} compression ratio")
        log.info("with $bitsPerSymbol bits/symbol")


        originalFiles.filter { it.key.isFile }.forEach { original ->
            if (filterFile.isNullOrEmpty()) {
                val encodedFile =
                    encodedFiles.filterKeys { it.nameWithoutExtension == original.key.nameWithoutExtension }
                val bitsPerSymbolFile = (encodedFile.values.first() * 8).toDouble() / original.value.toDouble()

                log.info("File ${original.key.name}, size original: ${original.value}, size encoded: ${encodedFile.values.first()}, compression: ${(encodedFile.values.first().toDouble() / original.value.toDouble()) * 100}, bps: $bitsPerSymbolFile")
            } else {
                if (original.key.name == filterFile) {
                    val encodedFile =
                        encodedFiles.filterKeys { it.nameWithoutExtension == original.key.nameWithoutExtension }
                    val bitsPerSymbolFile = (encodedFile.values.first() * 8).toDouble() / original.value.toDouble()

                    log.info("File ${original.key.name}, size encoded: ${encodedFile.values.first()}, size original: ${original.value}, compression: ${(encodedFile.values.first().toDouble() / original.value.toDouble()) * 100}, bps: $bitsPerSymbolFile")
                }
            }
        }
        log.info("\n\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n")
    }

    fun mapFile(inputFile: String, outputFile: String) {
        val mapping = getByteMapping()
        mapFile(inputFile, outputFile, mapping)
    }

    fun mapFile(inputFile: String, outputFile: String, mapping: Map<Byte, Byte>) {
        BitStream(File(inputFile).openBinaryStream(true)).use { streamIn ->
            BitStream(File(outputFile).openBinaryStream(false)).use { streamOut ->
                while (streamIn.position < streamIn.size) {
                    val currentByte = streamIn.readByte()
                    streamOut.write(mapping.getOrElse(currentByte) { throw IllegalArgumentException("No mapping found for byte $currentByte!") })
                }
            }
        }
    }

}