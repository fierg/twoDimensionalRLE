package edu.ba.twoDimensionalRLE.encoder.rle


import de.jupf.staticlog.Log
import de.jupf.staticlog.core.LogLevel
import edu.ba.twoDimensionalRLE.encoder.Encoder
import edu.ba.twoDimensionalRLE.extensions.pow
import edu.ba.twoDimensionalRLE.model.DataChunk
import edu.ba.twoDimensionalRLE.tranformation.BurrowsWheelerTransformation
import edu.ba.twoDimensionalRLE.tranformation.modified.BurrowsWheelerTransformationModified
import kanzi.SliceByteArray
import kanzi.transform.BWTS
import loggersoft.kotlin.streams.BitStream
import loggersoft.kotlin.streams.openBinaryStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
class StringRunLengthEncoder : Encoder {

    private val log = Log.kotlinInstance()
    private val DEBUG = false

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
        if (!DEBUG) log.logLevel = LogLevel.INFO
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

    override fun encode(
        inputFile: String, outputFile: String,
        applyByteMapping: Boolean,
        applyBurrowsWheelerTransformation: Boolean, byteArraySize: Int, bitsPerRLENumber: Int
    ) {
        log.info("Encoding file $inputFile with regular rle. Output file will be at $outputFile")
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
        log.debug("Finished encoding.")
    }

    fun encodeSimpleBinaryRLE(inputFile: String, outputFile: String, bitPerRun: Int) {
        var lastSeenBit = false
        var counter = 0
        val maxLength = 2.pow(bitPerRun) - 1

        log.info("Encoding $inputFile with simple binary rle and $bitPerRun bits per run...")

        BitStream(File(outputFile).openBinaryStream(false)).use { stream ->
            File(inputFile).readBytes().forEach { byte ->
                byte.toUByte().toInt().toString(2).padStart(8, '0').forEach { char ->
                    val currentBit = char == '1'

                    if (lastSeenBit == currentBit) {
                        if (++counter == maxLength) {
                            writeRunToStream(counter, stream, bitPerRun)
                            writeRunToStream(0,stream,bitPerRun)
                            counter = 0
                        }
                    } else {
                        if (counter > 0) {
                            writeRunToStream(counter, stream, bitPerRun)
                            counter = 1
                        } else {
                            counter++
                        }
                        lastSeenBit = !lastSeenBit
                    }
                }
            }
        }
    }

    fun encodeVarLength(
        inputFile: String, outputFile: String,
        bitPerRun: Int,
        applyByteMapping: Boolean,
        applyBurrowsWheelerTransformation: Boolean,
        applyBurrowsWheelerTransformationS: Boolean,
        chunkSize: Int
    ) {
        log.info("Encoding file $inputFile with byte wise rle and $bitPerRun. Output file will be at $outputFile")

        if (applyBurrowsWheelerTransformation && applyBurrowsWheelerTransformationS) throw IllegalArgumentException("Unable to use both BWT impls at the same time!")
        val input = File(inputFile)
        var transformedFile: String? = null
        val output = File(outputFile)
        output.createNewFile()
        var chunkSize = chunkSize

        if (applyBurrowsWheelerTransformationS) {
            log.debug("Applying bijective Burrows Wheeler Transformation to file...")
            val bwts = BWTS()
            transformedFile = "${outputFile}_bwt_tmp"
            val buf1 = File(inputFile).readBytes()
            val buf2 = ByteArray(buf1.size)
            val sa1 = SliceByteArray(buf1, 0)
            val sa2 = SliceByteArray(buf2, 0)
            bwts.forward(sa1, sa2)
            File(transformedFile).writeBytes(sa2.array)
        }

        if (applyBurrowsWheelerTransformation) {
            chunkSize -= 2
        }

        var chunks = DataChunk.readChunksFromFile(
            if (applyBurrowsWheelerTransformationS) transformedFile!! else inputFile,
            chunkSize,
            log
        )

        if (applyBurrowsWheelerTransformation) {
            val bwt =
                BurrowsWheelerTransformationModified()
            log.debug("Performing burrows wheeler transformation on all chunks...")
            chunks = bwt.performModifiedBurrowsWheelerTransformationOnAllChunks(chunks)
        }

        var lastSeenByte = 0.toByte()
        var counter = 0
        val maxLength = 2.pow(bitPerRun) - 1

        BitStream(output.openBinaryStream(false)).use { stream ->
            chunks.forEach { chunk ->
                chunk.bytes.forEach { byte ->
                    if (lastSeenByte == byte) {
                        if (++counter == maxLength) {
                            writeByteToStream(lastSeenByte, stream)
                            writeRunToStream(counter, stream, bitPerRun)
                            counter = 0
                        }
                    } else {
                        if (counter > 0) {
                            writeByteToStream(lastSeenByte, stream)
                            writeRunToStream(counter, stream, bitPerRun)
                            counter = 1
                            lastSeenByte = byte
                        } else {
                            counter++
                            lastSeenByte = byte
                        }
                    }
                }
            }
            if (counter != 0) {
                writeByteToStream(lastSeenByte, stream)
                writeRunToStream(counter, stream, bitPerRun)
            }
        }
        log.debug("Finished encoding.")
    }


    private fun writeByteToStream(char: Byte, stream: BitStream) {
        char.toUByte().toInt().toString(2).padStart(8, '0').reversed().forEach {
            stream += when (it) {
                '0' -> false
                '1' -> true
                else -> throw IllegalArgumentException()
            }
        }
    }

    override fun decode(
        inputFile: String, outputFile: String,
        applyByteMapping: Boolean,
        applyBurrowsWheelerTransformation: Boolean, byteArraySize: Int, bitsPerRLENumber: Int
    ) {
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
        log.debug("Finished decoding $input to $output.")
    }


    fun decodeVarLength(
        inputFile: String,
        outputFile: String,
        bitPerRun: Int,
        applyByteMapping: Boolean,
        applyBurrowsWheelerTransformation: Boolean,
        applyBurrowsWheelerTransformationS: Boolean,
        chunkSize: Int
    ) {
        val input = File(inputFile)
        val output = File(outputFile)
        var currentBytes = mutableListOf<Byte>()
        var chunks = mutableListOf<DataChunk>()

        log.info("Decoding $input with  $bitPerRun rle bits per number ...")
        BitStream(input.openBinaryStream(true)).use { stream ->
            var counter = 0
            var char: Byte
            while (stream.bitPosition < (stream.size - 1) * 8) {

                char = stream.readBits(8, false).toByte()
                counter = stream.readBits(bitPerRun, false).toInt()

                for (i in 0 until counter) {
                    currentBytes.add(char)
                    if (currentBytes.size == chunkSize) {
                        chunks.add(DataChunk(currentBytes.toByteArray()))
                        currentBytes = mutableListOf()
                    }
                }
            }
            if (currentBytes.size > 0) {
                chunks.add(DataChunk(currentBytes.toByteArray()))
            }
        }
        log.debug("Decoded into ${chunks.size} chunks of size $chunkSize bytes.")


        if (applyBurrowsWheelerTransformation) {
            val bwt = BurrowsWheelerTransformation()
            log.debug("Performing inverse burrows wheeler transformation on all chunks...")
            chunks = bwt.invertTransformationParallel(chunks).toMutableList()
        }

        chunks.forEach { it.appendCurrentChunkToFile(outputFile) }
        var transformedFile: String? = null
        if(applyBurrowsWheelerTransformationS) {
            log.debug("Applying inverse Burrows Wheeler Transformation to file...")
            transformedFile = "${output}_bwt"
            val bwt = BWTS()
            val buf1 = File(inputFile).readBytes()
            val buf2 = ByteArray(buf1.size)
            val sa1 = SliceByteArray(buf1, 0)
            val sa2 = SliceByteArray(buf2, 0)
            bwt.inverse(sa1,sa2)
            File(transformedFile).writeBytes(sa2.array)
        }
        log.debug("Finished decoding $input to ${if (transformedFile.isNullOrEmpty()) output else transformedFile}.")
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