package edu.ba.twoDimensionalRLE.encoder.rle


import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.encoder.Encoder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class StringRunLengthEncoder : Encoder {

    private val log = Log.kotlinInstance()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }

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
                sb.append(group.groups[3]!!.value)
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

    override fun encode(inputFile: String, outputFile: String) {
        log.info("Starting to encode file $inputFile with regular rle. Output file will be at $outputFile")
        val input = File(inputFile)
        val output = File(outputFile)
        output.createNewFile()
        var lastSeenByte = 0.toByte()
        var counter = 0

        FileOutputStream(output, true).buffered().use { writer ->
            FileInputStream(input).buffered().readBytes().forEach { byte ->
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
        log.info("Finished encoding.")
    }

    @ExperimentalUnsignedTypes
    override fun decode(inputFile: String, outputFile: String) {
        val input = File(inputFile)
        val output = File(outputFile)

        log.info("Starting to decode $input...")
        FileOutputStream(output, true).buffered().use { writer ->
            var counter = 0
            var char: Char
            var count = 0
            FileInputStream(input).buffered().readBytes().forEach { byte ->
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
        log.info("Finished decoding $input to $output.")
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


