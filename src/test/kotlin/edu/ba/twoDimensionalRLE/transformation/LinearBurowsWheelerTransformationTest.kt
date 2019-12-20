package edu.ba.twoDimensionalRLE.transformation

import edu.ba.twoDimensionalRLE.tranformation.BurrowsWheelerTransformationLinearTime
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class LinearBurowsWheelerTransformationTest {

    @Test
    @Order(1)
    fun simpleTest() {
        val bwtl = BurrowsWheelerTransformationLinearTime()

        bwtl.transform("zzatestatestatest")
    }
}