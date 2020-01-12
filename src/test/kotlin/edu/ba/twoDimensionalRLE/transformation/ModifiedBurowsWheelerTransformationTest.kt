package edu.ba.twoDimensionalRLE.transformation

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.tranformation.bijectiveJavaWrapper.BWTSWrapper
import edu.ba.twoDimensionalRLE.tranformation.modified.BurrowsWheelerTransformationModified
import kanzi.SliceByteArray
import kanzi.transform.BWTS
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.io.File


@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ModifiedBurowsWheelerTransformationTest {
    private var log = Log.kotlinInstance()
    private val word = "abraca"
    private val longWord =
        "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."
    private val byteArray = "testatestatest".toByteArray()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }

    }

    companion object {
        private const val fileToEncodeSmall = "testFile_small.txt"
        private const val fileToEncode = "t8.shakespeare_medium.txt"
        private const val encodeFolder = "data/encoded/bwts"
        private const val decodeFolder = "data/decoded/bwts"
    }


    @Test
    @Order(1)
    fun simpleTest() {
        val bwtl =
            BurrowsWheelerTransformationModified()
        log.info("Starting transformation on $word ...")
        val result = bwtl.transform(word)

        log.info("Result: ${result.first}, Index: ${result.second}")
    }

    @Test
    @Order(2)
    fun simpleTestDecode() {
        val bwtl =
            BurrowsWheelerTransformationModified()
        val result = bwtl.transform(word)
        log.info("Starting inverse transformation on ${result.first} ...")

        val inverted = bwtl.inverseTransform(result.first, result.second)

        log.info("Result: $inverted")
    }

    @Test
    @Order(3)
    fun longTestDecode() {
        val bwtl =
            BurrowsWheelerTransformationModified()
        log.info("Starting transformation on $longWord ...")
        val result = bwtl.transform(longWord)
        log.info("Result: ${result.first}")

        log.info("Starting inverse transformation...")

        val inverted = bwtl.inverseTransform(result.first, result.second)

        log.info("Result: $inverted")
    }


    @Test
    @Order(4)
    fun byteArrayTestDecode() {
        val bwtl =
            BurrowsWheelerTransformationModified()
        log.info("Starting bwt on byteArray $byteArray (${byteArray.decodeToString()})")
        val result = bwtl.transformByteArray(byteArray)

        log.info("Result: ${result.first} (${result.first.decodeToString()})")
        log.info("Starting inverse transformation on...")

        val inverted = bwtl.inverseTransformByteArray(result.first, result.second)

        log.info("Result: $inverted (${inverted.decodeToString()})")
    }

    @Test
    @Order(5)
    fun fileTestEncodeAndDecode() {
        val bwts = BWTS()
        log.info("Starting bwt on file data/$fileToEncode")

        val buf1 = File("data/$fileToEncode").readBytes()
        val buf2 = ByteArray(buf1.size)
        val sa1 = SliceByteArray(buf1, 0)
        val sa2 = SliceByteArray(buf2, 0)
        bwts.forward(sa1, sa2)
        File("${encodeFolder}/${fileToEncode}_bwts").writeBytes(sa2.array)


        log.info("Result:")
        println(File("${encodeFolder}/${fileToEncode}_bwts").readText())


        log.info("Starting inverse transformation on ${encodeFolder}/${fileToEncode}_bwts ...")

        val buf3 = File("${encodeFolder}/${fileToEncode}_bwts").readBytes()
        val buf4 = ByteArray(buf3.size)
        val sa3 = SliceByteArray(buf3, 0)
        val sa4 = SliceByteArray(buf4, 0)

        bwts.inverse(sa3, sa4)
        File("${decodeFolder}/${fileToEncode}").writeBytes(sa4.array)

        log.info("Result:")
        println(File("${decodeFolder}/${fileToEncode}").readText())


    }

    @Test
    @Order(6)
    fun fileTestEncodeAndDecodeSmall() {
        val bwts = BWTSWrapper()

        bwts.transformFile(File("data/$fileToEncodeSmall"), File("${encodeFolder}/${fileToEncodeSmall}_bwts"))

        bwts.invert(File("${encodeFolder}/${fileToEncodeSmall}_bwts"), File("${decodeFolder}/${fileToEncodeSmall}"))
    }

}