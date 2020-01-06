package edu.ba.twoDimensionalRLE.transformation

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.tranformation.BurrowsWheelerTransformationLinearTime
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder


@ExperimentalStdlibApi
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class LinearBurowsWheelerTransformationTest {
    private var log = Log.kotlinInstance()
    private val word ="abraca"
    private val longWord = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }


    @Test
    @Order(1)
    fun simpleTest() {
        val bwtl = BurrowsWheelerTransformationLinearTime()
        log.info("Strarting transformation on $word ...")
        val result = bwtl.transform(word)

        log.info("Result: ${result.first}, Index: ${result.second}")
    }

    @Test
    @Order(2)
    fun simpleTestDecode() {
        val bwtl = BurrowsWheelerTransformationLinearTime()
        val result = bwtl.transform(word)
        log.info("Strarting inverse transformation on ${result.first} ...")

        val inverted = bwtl.inverseTransform(result.first, result.second)

        log.info("Result: $inverted")
    }

    @Test
    @Order(3)
    fun longTestDecode() {
        val bwtl = BurrowsWheelerTransformationLinearTime()
        val result = bwtl.transform(longWord)
        log.info("Strarting inverse transformation on ${result.first} ...")

        val inverted = bwtl.inverseTransform(result.first, result.second)

        log.info("Result: $inverted")
    }


}