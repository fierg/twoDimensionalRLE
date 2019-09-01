package edu.ba.twoDimensionalRLE.encoder

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.text.StringBuilder

class StringRunLengthEncoder : Encoder {
    override fun decode(file: String) {
        val inputFile = File(file)
        val outputFile = File("data/decoded/${inputFile.nameWithoutExtension}.txt")
        if (outputFile.exists()) outputFile.delete()
        val sb = StringBuilder()

        FileInputStream(inputFile).bufferedReader().use { reader ->
            reader.forEachLine { line ->
                sb.append(runLengthDecodingString(line))
                sb.append("\n")
            }
        }
        FileOutputStream(outputFile, true).bufferedWriter().use { writer -> writer.write(sb.toString()) }
    }

    private fun runLengthDecodingString(line: String): String {
        val sb = StringBuilder()
        if (line.isBlank()) return "\n"
        val regex = Regex("((\\d+) (.))")
        regex.findAll(line).iterator().forEach { group ->
            for (i in 0 until group.groups[2]!!.value.toInt()) {
                sb.append(group.groups!![3]!!.value)
            }
        }
        return sb.toString()
    }

    override fun encode(file: String) {
        val inputFile = File(file)
        val outputFile = File("data/encoded/${inputFile.nameWithoutExtension}_rle.txt")

        if (outputFile.exists()) {
            outputFile.delete()
        } else {
            File("data/encoded").mkdir()
            File("data/decoded").mkdir()
        }
        val sb = StringBuilder()

        FileInputStream(inputFile).bufferedReader().use { reader ->
            reader.forEachLine { line ->
                sb.append(runLengthEncodingString(line))
                sb.append("\n")
            }
        }
        FileOutputStream(outputFile, true).bufferedWriter().use { writer -> writer.write(sb.toString()) }
    }

    private tailrec fun runLengthEncodingStringRec(text: String, prev: String = ""): String {
        if (text.isEmpty()) {
            return prev
        }
        val initialChar = text[0]
        val count = text.takeWhile { it == initialChar }.count()
        return runLengthEncodingStringRec(text.substring(count), "$prev $count $initialChar")
    }

    fun runLengthEncodingString(text: String): String {
        return runLengthEncodingStringRec(text).trim()
    }


}


