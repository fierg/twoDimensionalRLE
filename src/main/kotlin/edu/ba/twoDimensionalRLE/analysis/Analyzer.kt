package edu.ba.twoDimensionalRLE.analysis

import java.io.File

class Analyzer {

    private val encodingOccurrenceMap = mutableMapOf<Int, Int>()
    private val byteOccurrenceMap = mutableMapOf<Byte, Int>()
    private var byteMapping = mutableMapOf<Byte, Byte>()

    fun printFileComparison(source: File, target: File) {
        println("Encoded File has a file size of ${target.length() / 1000000.toDouble()} MB")
        println("${(target.length() / source.length().toDouble())} times the original size")
    }

    fun printOccurrenceMap() {
        println("Repetition counts: ")
        encodingOccurrenceMap.keys.distinct().sorted().forEach {
            println("$it ${encodingOccurrenceMap[it]}")
        }
    }

    fun printByteOccurrence() {
        byteOccurrenceMap.values.distinct().sortedDescending().forEach {
            byteOccurrenceMap.filter { entry -> entry.value == it }.toSortedMap().forEach { (byte, int) ->
                println("byte $byte occurred $int times in the original stream")
            }
        }
    }

    private fun createByteMapping(): MutableMap<Byte, Byte> {
        val resultMap = mutableMapOf<Byte, Byte>()
        var counter = 0

        byteOccurrenceMap.values.distinct().sortedDescending().forEach { key ->
            byteOccurrenceMap.filter { it.value == key }.keys.sorted().forEach { byte ->
                println("mapping entry: <${byte} -- ${counter.toByte()}>")
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

}