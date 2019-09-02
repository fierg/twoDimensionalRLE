package edu.ba.twoDimensionalRLE.encoder


import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.text.StringBuilder

class StringRunLengthEncoder : Encoder {

    companion object {
        private const val maxLength = 255
    }

    fun decodeReadable(file: String) {
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

    fun encodeReadabe(file: String) {
        val inputFile = File(file)
        val outputFile = File("data/encoded/${inputFile.nameWithoutExtension}_rle.txt")

        val sb = StringBuilder()

        FileInputStream(inputFile).bufferedReader().use { reader ->
            reader.forEachLine { line ->
                sb.append(runLengthEncodingString(line))
                sb.append("\n")
            }
        }
        FileOutputStream(outputFile, true).bufferedWriter().use { writer -> writer.write(sb.toString()) }
    }

    override fun encode(file: String, outputFile: String) {
        val inputFile = File(file)
        val outputFile = File(outputFile)
        outputFile.createNewFile()
        var lastSeenByte = 0.toByte()
        var counter = 0

        FileOutputStream(outputFile, true).buffered().use { writer ->
            FileInputStream(inputFile).buffered().readAllBytes().forEach { byte ->
                if (lastSeenByte == byte) {
                    if (++counter == maxLength) {
                        writer.write(writeAsTwoByte(lastSeenByte, counter))
                        counter = 0
                    }
                } else {
                    if (counter > 0) {
                        writer.write(writeAsTwoByte(lastSeenByte, counter))
                        counter = 1
                        lastSeenByte = byte
                    } else {
                        counter++
                        lastSeenByte = byte
                    }
                }
            }
            writer.write(writeAsTwoByte(lastSeenByte, counter))
        }
    }

    override fun decode(file: String, outputFile: String) {
        val inputFile = File(file)
        val outputFile = File(outputFile)

        FileOutputStream(outputFile, true).buffered().use { writer ->
            var counter = 0
            var char: Char
            var count = 0
            FileInputStream(inputFile).buffered().readAllBytes().forEach { byte ->
                if (++counter % 2 == 0) {
                    char = byte.toChar()
                    for (i in 0 until count) {
                        writer.write(char.toInt())
                    }
                    counter = 0
                } else {
                    count = byte.toUByte().toInt()
                }


            }
        }
    }

    private fun writeAsTwoByte(lastSeenByte: Byte, count: Int): ByteArray {
        require(count <= maxLength) { "Count exceeded max count length. if this occurs more often, consider increasing the max length property. (experimental)" }
        val byteArray = ByteArray(2)
        byteArray[0] = count.toByte()
        byteArray[1] = lastSeenByte
        return byteArray
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


