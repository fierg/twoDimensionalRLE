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
        occurrenceMap.keys.sorted().forEach { println("$it ${occurrenceMap[it]}") }
    }

    fun printByteOccurrence() {
        byteOccurrenceMap.values.sortedDescending().forEach {
            byteOccurrenceMap.filter { entry -> entry.value == it }.toSortedMap().forEach { (byte, int) -> println("byte $byte occurred $int times in the original stream") }
        }
    }

    fun createByteMapping() {
        val resultMap = mutableMapOf<Byte,Byte>()
        var counter = 0
        byteOccurrenceMap.values.sortedDescending().forEach {
            byteOccurrenceMap.filter { entry -> entry.value == it }.toSortedMap().forEach { byte -> resultMap[byte.key] = counter++.toByte() }
        }
        resultMap.forEach { (inByte, outByte) -> println("mapping entry: <${inByte} -- ${outByte}>") }
    }
}