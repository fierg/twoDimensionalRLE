package edu.ba.twoDimensionalRLE.analysis

import java.io.File

class Analyzer {

    internal val occurrenceMap = mutableMapOf<Int, Int>()
    internal val byteOccurrenceMap = mutableMapOf<Byte, Int>()

    fun printFileComparison(source: File, target: File) {
        println("Encoded File has a file size of ${target.length() / 1000000.toDouble()} MB")
        println("${(target.length() / source.length().toDouble())} times the original size")
    }

    fun printOccurrenceMap() {
        println("Repetition counts: ")
        occurrenceMap.keys.distinct().sorted().forEach {
            println("$it ${occurrenceMap[it]}")
        }
    }

    fun printByteOccurrence() {
        byteOccurrenceMap.values.distinct().sortedDescending().forEach {
            byteOccurrenceMap.filter { entry -> entry.value == it }.toSortedMap().iterator()
                .forEach { (byte, int) -> println("byte $byte occurred $int times in the original stream") }
        }
    }

    fun createByteMapping(): MutableMap<Byte, Byte> {
        val resultMap = mutableMapOf<Byte, Byte>()
        var counter = 0
        byteOccurrenceMap.values.distinct().sortedDescending().forEach {
            byteOccurrenceMap.filter { entry -> entry.value == it }.forEach { byte ->
                    resultMap[byte.key] = counter++.toByte()
//                    println("mapping entry: <${byte} -- ${counter++.toByte()}>")
                }
        }
        return resultMap
    }
}