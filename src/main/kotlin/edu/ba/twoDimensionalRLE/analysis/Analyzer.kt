package edu.ba.twoDimensionalRLE.analysis

import java.io.File

class Analyzer {

    internal val occurrenceMap = mutableMapOf<Int, Int>()

    fun printFileComparison(source: File, target: File) {
        println("Encoded File has a file size of ${target.length() / 1000000.toDouble()} MB")
        println("${(target.length() / source.length().toDouble())} times the original size")
    }

    fun printOccurrenceMap() {
        println("Occurrence of repetition counts: ")
        occurrenceMap.keys.sorted().forEach { println("$it ${occurrenceMap[it]}") }
    }
}