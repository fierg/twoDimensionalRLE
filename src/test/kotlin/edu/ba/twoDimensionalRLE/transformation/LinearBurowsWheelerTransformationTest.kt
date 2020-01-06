package edu.ba.twoDimensionalRLE.transformation

import de.jupf.staticlog.Log
import edu.ba.twoDimensionalRLE.tranformation.BurrowsWheelerTransformationLinearTime
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class LinearBurowsWheelerTransformationTest {
    private var log = Log.kotlinInstance()

    init {
        log.newFormat {
            line(date("yyyy-MM-dd HH:mm:ss"), space, level, text("/"), tag, space(2), message, space(2))
        }
    }


    @Test
    @Order(1)
    fun simpleTest() {
        val bwtl = BurrowsWheelerTransformationLinearTime()
        val result = bwtl.transform("bcbccbcbcabbaaba")

        log.info("Result: ${result.first}, Index: ${result.second}")
    }
}